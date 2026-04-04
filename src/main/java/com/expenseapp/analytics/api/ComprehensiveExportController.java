package com.expenseapp.analytics.api;

import com.expenseapp.analytics.dto.AnalyticsResponse;
import com.expenseapp.analytics.service.AnalyticsService;
import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.user.service.UserService;
import com.expenseapp.user.domain.User;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * REST controller for comprehensive financial report export operations.
 * Generates a multi-sheet Excel report with Dashboard, Assets, Liabilities, Income, Expenses, and Summary.
 */
@RestController
@RequestMapping("/api/export")
@PreAuthorize("isAuthenticated()")
public class ComprehensiveExportController {

    private final AnalyticsService analyticsService;
    private final UserService userService;

    public ComprehensiveExportController(AnalyticsService analyticsService, UserService userService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
    }

    /**
     * Export comprehensive financial report to Excel format for a specific month.
     * Creates a multi-sheet Excel file matching the monthly expense report template format.
     *
     * @param yearMonth the year and month for the report (e.g., "2024-03")
     * @param authentication the current authentication
     * @return Excel file as response
     */
    @GetMapping("/comprehensive/report/excel")
    public ResponseEntity<byte[]> exportComprehensiveReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            Authentication authentication) throws IOException {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        // Calculate date range for the month
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get analytics data for the month
        AnalyticsResponse analytics = analyticsService.getUserAnalytics(user, startDate, endDate);

        // Create Excel workbook
        Workbook workbook = new XSSFWorkbook();

        // Create sheets matching the monthly expense report template format
        createSummarySheet(workbook, yearMonth, analytics);
        createMonthlySheet(workbook, yearMonth, analytics);

        // Write to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        byte[] excelBytes = outputStream.toByteArray();

        // Set response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", 
            "Monthly_Expense_Report_" + yearMonth.toString() + ".xlsx");
        headers.setContentLength(excelBytes.length);

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    /**
     * Export financial dashboard to PDF format for a specific month.
     * Creates a single-page PDF document that matches the Financial Dashboard view exactly.
     *
     * @param yearMonth the year and month for the report (e.g., "2024-03")
     * @param authentication the current authentication
     * @return PDF file as response
     */
    @GetMapping("/comprehensive/report/pdf")
    public ResponseEntity<byte[]> exportDashboardPDF(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            Authentication authentication) throws DocumentException, IOException {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        // Calculate date range for the month
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get analytics data for the month
        AnalyticsResponse analytics = analyticsService.getUserAnalytics(user, startDate, endDate);

        // Create PDF document - single page layout
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();

        // Create main container table for single-page layout
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);
        mainTable.setSpacingBefore(10f);
        mainTable.setSpacingAfter(10f);

        // Header section
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        
        // Title
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new BaseColor(0, 0, 139));
        Paragraph title = new Paragraph("Financial Dashboard", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        PdfPCell titleCell = new PdfPCell(title);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPaddingBottom(5f);
        headerTable.addCell(titleCell);

        // Date range
        com.itextpdf.text.Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(128, 128, 128));
        Paragraph dateRange = new Paragraph("Report Period: " + 
            startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + 
            " to " + endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), subtitleFont);
        dateRange.setAlignment(Element.ALIGN_CENTER);
        PdfPCell dateCell = new PdfPCell(dateRange);
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setPaddingBottom(15f);
        headerTable.addCell(dateCell);

        PdfPCell headerCell = new PdfPCell(headerTable);
        headerCell.setBorder(Rectangle.NO_BORDER);
        mainTable.addCell(headerCell);

        // Summary cards section - 2x2 grid
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{1, 1});
        summaryTable.setSpacingAfter(15f);

        // Create summary cards in 2x2 grid
        // Row 1: Total Income and Total Expenses
        addSummaryCard(summaryTable, "Total Income", 
                      formatCurrency(analytics.getTotalIncome()), 
                      new BaseColor(0, 176, 80)); // Green
        addSummaryCard(summaryTable, "Total Expenses", 
                      formatCurrency(analytics.getTotalExpenses()), 
                      new BaseColor(255, 192, 0)); // Orange

        // Row 2: Net Balance and Save Rate
        addSummaryCard(summaryTable, "Net Balance", 
                      formatCurrency(analytics.getTotalIncome().subtract(analytics.getTotalExpenses())), 
                      new BaseColor(0, 112, 192)); // Blue
        addSummaryCard(summaryTable, "Save Rate", 
                      calculateSaveRate(analytics).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%", 
                      new BaseColor(255, 192, 203)); // Pink

        PdfPCell summaryCell = new PdfPCell(summaryTable);
        summaryCell.setBorder(Rectangle.NO_BORDER);
        summaryCell.setPaddingBottom(10f);
        mainTable.addCell(summaryCell);

        // Charts section - side by side
        PdfPTable chartsTable = new PdfPTable(2);
        chartsTable.setWidthPercentage(100);
        chartsTable.setWidths(new float[]{1, 1});
        chartsTable.setSpacingAfter(15f);

        // Left chart: Spending by Category
        PdfPTable categoryChartTable = new PdfPTable(1);
        categoryChartTable.setWidthPercentage(100);
        
        Paragraph categoryTitle = new Paragraph("Spending by Category", 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(0, 0, 139)));
        PdfPCell categoryTitleCell = new PdfPCell(categoryTitle);
        categoryTitleCell.setBorder(Rectangle.NO_BORDER);
        categoryTitleCell.setPaddingBottom(5f);
        categoryChartTable.addCell(categoryTitleCell);

        // Category breakdown table
        PdfPTable categoryTable = new PdfPTable(3);
        categoryTable.setWidthPercentage(100);
        categoryTable.setWidths(new float[]{2, 1, 1});
        categoryTable.setSpacingAfter(5f);

        // Add headers
        addTableHeader(categoryTable, "Category", new BaseColor(0, 112, 192));
        addTableHeader(categoryTable, "Amount", new BaseColor(0, 112, 192));
        addTableHeader(categoryTable, "Percentage", new BaseColor(0, 112, 192));

        // Add category data (limit to top 5 categories for space)
        int categoryCount = 0;
        for (var category : analytics.getSpendingByCategory()) {
            if (categoryCount >= 5) break; // Limit to top 5 categories
            addTableCell(categoryTable, category.getCategoryName(), Element.ALIGN_LEFT, false);
            addTableCell(categoryTable, formatCurrency(category.getTotalAmount()), Element.ALIGN_RIGHT, true);
            addTableCell(categoryTable, String.format("%.1f", category.getPercentage().doubleValue()) + "%", 
                        Element.ALIGN_RIGHT, false);
            categoryCount++;
        }

        // Add "Other" category if there are more than 5 categories
        if (analytics.getSpendingByCategory().size() > 5) {
            BigDecimal otherTotal = BigDecimal.ZERO;
            BigDecimal otherPercentage = BigDecimal.ZERO;
            for (int i = 5; i < analytics.getSpendingByCategory().size(); i++) {
                otherTotal = otherTotal.add(analytics.getSpendingByCategory().get(i).getTotalAmount());
                otherPercentage = otherPercentage.add(BigDecimal.valueOf(analytics.getSpendingByCategory().get(i).getPercentage()));
            }
            addTableCell(categoryTable, "Other", Element.ALIGN_LEFT, false);
            addTableCell(categoryTable, formatCurrency(otherTotal), Element.ALIGN_RIGHT, true);
            addTableCell(categoryTable, String.format("%.1f", otherPercentage) + "%", 
                        Element.ALIGN_RIGHT, false);
        }

        PdfPCell categoryTableCell = new PdfPCell(categoryTable);
        categoryTableCell.setBorder(Rectangle.NO_BORDER);
        categoryChartTable.addCell(categoryTableCell);

        PdfPCell categoryChartCell = new PdfPCell(categoryChartTable);
        categoryChartCell.setBorder(Rectangle.BOX);
        categoryChartCell.setBorderWidth(1f);
        categoryChartCell.setBorderColor(new BaseColor(200, 200, 200));
        categoryChartCell.setPadding(8f);
        chartsTable.addCell(categoryChartCell);

        // Right chart: Monthly Income vs Expenses
        PdfPTable monthlyChartTable = new PdfPTable(1);
        monthlyChartTable.setWidthPercentage(100);
        
        Paragraph monthlyTitle = new Paragraph("Monthly Overview", 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(0, 0, 139)));
        PdfPCell monthlyTitleCell = new PdfPCell(monthlyTitle);
        monthlyTitleCell.setBorder(Rectangle.NO_BORDER);
        monthlyTitleCell.setPaddingBottom(5f);
        monthlyChartTable.addCell(monthlyTitleCell);

        // Monthly data table
        PdfPTable monthlyTable = new PdfPTable(4);
        monthlyTable.setWidthPercentage(100);
        monthlyTable.setWidths(new float[]{1, 1, 1, 1});
        monthlyTable.setSpacingAfter(5f);

        // Add headers
        addTableHeader(monthlyTable, "Month", new BaseColor(0, 112, 192));
        addTableHeader(monthlyTable, "Income", new BaseColor(0, 176, 80));
        addTableHeader(monthlyTable, "Expenses", new BaseColor(255, 192, 0));
        addTableHeader(monthlyTable, "Net", new BaseColor(0, 112, 192));

        // Add monthly data
        addTableCell(monthlyTable, yearMonth.toString(), Element.ALIGN_CENTER, false);
        addTableCell(monthlyTable, formatCurrency(analytics.getTotalIncome()), Element.ALIGN_RIGHT, true);
        addTableCell(monthlyTable, formatCurrency(analytics.getTotalExpenses()), Element.ALIGN_RIGHT, true);
        addTableCell(monthlyTable, formatCurrency(analytics.getTotalIncome().subtract(analytics.getTotalExpenses())), 
                    Element.ALIGN_RIGHT, true);

        PdfPCell monthlyTableCell = new PdfPCell(monthlyTable);
        monthlyTableCell.setBorder(Rectangle.NO_BORDER);
        monthlyChartTable.addCell(monthlyTableCell);

        PdfPCell monthlyChartCell = new PdfPCell(monthlyChartTable);
        monthlyChartCell.setBorder(Rectangle.BOX);
        monthlyChartCell.setBorderWidth(1f);
        monthlyChartCell.setBorderColor(new BaseColor(200, 200, 200));
        monthlyChartCell.setPadding(8f);
        chartsTable.addCell(monthlyChartCell);

        PdfPCell chartsCell = new PdfPCell(chartsTable);
        chartsCell.setBorder(Rectangle.NO_BORDER);
        chartsCell.setPaddingBottom(10f);
        mainTable.addCell(chartsCell);

        // Footer section
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);
        
        Paragraph footer = new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), 
            FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(128, 128, 128)));
        footer.setAlignment(Element.ALIGN_CENTER);
        PdfPCell footerCell = new PdfPCell(footer);
        footerCell.setBorder(Rectangle.NO_BORDER);
        footerCell.setPaddingTop(10f);
        footerTable.addCell(footerCell);

        PdfPCell footerContainerCell = new PdfPCell(footerTable);
        footerContainerCell.setBorder(Rectangle.NO_BORDER);
        mainTable.addCell(footerContainerCell);

        // Add main table to document
        document.add(mainTable);

        document.close();

        byte[] pdfBytes = outputStream.toByteArray();

        // Set response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
            "Financial_Dashboard_" + yearMonth.toString() + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Create the main Dashboard sheet with visual summary cards and charts.
     * This creates a single-sheet Excel file that matches the Financial Dashboard view exactly.
     */
    private void createDashboardSheet(Workbook workbook, YearMonth yearMonth, AnalyticsResponse analytics) {
        Sheet sheet = workbook.createSheet("Financial Dashboard");

        // Calculate date range for the month
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Create styles
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle footerStyle = createFooterStyle(workbook);
        CellStyle cardTitleStyle = createCardTitleStyle(workbook, IndexedColors.LIGHT_GREEN);
        CellStyle cardValueStyle = createCardValueStyle(workbook, IndexedColors.LIGHT_GREEN);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        int rowIndex = 0;

        // Header section - matches dashboard header
        Row headerRow = sheet.createRow(rowIndex++);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Financial Dashboard");
        headerCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

        // Date range - matches dashboard date range display
        Row dateRow = sheet.createRow(rowIndex++);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Report Period: " + startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + 
                             " to " + endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 11));

        rowIndex += 2; // Add spacing

        // Summary Cards Section - matches dashboard summary cards
        Row cardsHeaderRow = sheet.createRow(rowIndex++);
        Cell cardsHeaderCell = cardsHeaderRow.createCell(0);
        cardsHeaderCell.setCellValue("Financial Summary");
        cardsHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 11));

        rowIndex += 2; // Add spacing for cards

        // Create summary cards - matches dashboard layout (4 cards in 2 rows)
        // Row 1: Total Income, Total Expenses, Net Balance, Save Rate
        createDashboardCard(sheet, rowIndex, 0, "Total Income", analytics.getTotalIncome(), 
                           IndexedColors.LIGHT_GREEN, cardTitleStyle, cardValueStyle);
        createDashboardCard(sheet, rowIndex, 3, "Total Expenses", analytics.getTotalExpenses(), 
                           IndexedColors.LIGHT_ORANGE, cardTitleStyle, cardValueStyle);
        createDashboardCard(sheet, rowIndex, 6, "Net Balance", analytics.getTotalIncome().subtract(analytics.getTotalExpenses()), 
                           IndexedColors.LIGHT_BLUE, cardTitleStyle, cardValueStyle);
        createDashboardCard(sheet, rowIndex, 9, "Save Rate", calculateSaveRate(analytics), 
                           IndexedColors.LIGHT_YELLOW, cardTitleStyle, cardValueStyle);

        rowIndex += 4; // Add spacing

        // Account Balances Section
        Row accountsHeaderRow = sheet.createRow(rowIndex++);
        Cell accountsHeaderCell = accountsHeaderRow.createCell(0);
        accountsHeaderCell.setCellValue("Account Balances");
        accountsHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 11));

        rowIndex += 1; // Add spacing

        // Account balances header
        Row accHeaderRow = sheet.createRow(rowIndex++);
        String[] accHeaders = {"Account Name", "Account Type", "Current Balance", "Status"};
        for (int i = 0; i < accHeaders.length; i++) {
            Cell cell = accHeaderRow.createCell(i);
            cell.setCellValue(accHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 4, 11)); // Merge remaining columns

        // Note about accounts (since we don't have real account data in analytics)
        Row noteRow = sheet.createRow(rowIndex++);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("Note: Account balances would be populated from user's actual accounts");
        noteCell.setCellStyle(dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 11));

        rowIndex += 2; // Add spacing

        // Spending by Category Section
        Row categoryHeaderRow = sheet.createRow(rowIndex++);
        Cell categoryHeaderCell = categoryHeaderRow.createCell(0);
        categoryHeaderCell.setCellValue("Spending by Category");
        categoryHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 11));

        rowIndex += 1; // Add spacing

        // Category spending header
        Row catHeaderRow = sheet.createRow(rowIndex++);
        String[] catHeaders = {"Category", "Amount", "Percentage", "Visual"};
        for (int i = 0; i < catHeaders.length; i++) {
            Cell cell = catHeaderRow.createCell(i);
            cell.setCellValue(catHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 4, 11)); // Merge remaining columns

        // Category spending data
        for (var category : analytics.getSpendingByCategory()) {
            Row catRow = sheet.createRow(rowIndex++);
            Cell catNameCell = catRow.createCell(0);
            catNameCell.setCellValue(category.getCategoryName());
            catNameCell.setCellStyle(dataStyle);

            Cell catAmountCell = catRow.createCell(1);
            catAmountCell.setCellValue(category.getTotalAmount().doubleValue());
            catAmountCell.setCellStyle(currencyStyle);

            Cell catPercentCell = catRow.createCell(2);
            catPercentCell.setCellValue(category.getPercentage().doubleValue() / 100.0); // Convert to decimal for Excel percentage
            catPercentCell.setCellStyle(createPercentageStyle(workbook));

            Cell catVisualCell = catRow.createCell(3);
            catVisualCell.setCellValue("Chart representation");
            catVisualCell.setCellStyle(dataStyle);
        }

        rowIndex += 2; // Add spacing

        // Monthly Income vs Expenses Section
        Row monthlyHeaderRow = sheet.createRow(rowIndex++);
        Cell monthlyHeaderCell = monthlyHeaderRow.createCell(0);
        monthlyHeaderCell.setCellValue("Monthly Income vs Expenses");
        monthlyHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 11));

        rowIndex += 1; // Add spacing

        // Monthly data header
        Row monHeaderRow = sheet.createRow(rowIndex++);
        String[] monHeaders = {"Month", "Income", "Expenses", "Net"};
        for (int i = 0; i < monHeaders.length; i++) {
            Cell cell = monHeaderRow.createCell(i);
            cell.setCellValue(monHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 4, 11)); // Merge remaining columns

        // Monthly data (current month)
        Row monRow = sheet.createRow(rowIndex++);
        Cell monMonthCell = monRow.createCell(0);
        monMonthCell.setCellValue(yearMonth.toString());
        monMonthCell.setCellStyle(dataStyle);

        Cell monIncomeCell = monRow.createCell(1);
        monIncomeCell.setCellValue(analytics.getTotalIncome().doubleValue());
        monIncomeCell.setCellStyle(currencyStyle);

        Cell monExpenseCell = monRow.createCell(2);
        monExpenseCell.setCellValue(analytics.getTotalExpenses().doubleValue());
        monExpenseCell.setCellStyle(currencyStyle);

        Cell monNetCell = monRow.createCell(3);
        monNetCell.setCellValue(analytics.getTotalIncome().subtract(analytics.getTotalExpenses()).doubleValue());
        monNetCell.setCellStyle(currencyStyle);

        rowIndex += 2; // Add spacing

        // Footer
        Row footerRow = sheet.createRow(rowIndex++);
        Cell footerCell = footerRow.createCell(0);
        footerCell.setCellValue("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        footerCell.setCellStyle(footerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 11));

        // Auto-size columns
        for (int i = 0; i < 12; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create the Assets sheet with detailed asset information.
     */
    private void createAssetsSheet(Workbook workbook, YearMonth yearMonth, AnalyticsResponse analytics) {
        Sheet sheet = workbook.createSheet("Assets");

        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);

        int rowIndex = 0;

        // Title
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Assets - " + yearMonth.toString());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowIndex += 2;

        // Headers
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Asset Name", "Type", "Value", "Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Sample asset data
        Object[][] assetData = {
            {"Savings Account", "Bank Account", 5000.00, "2024-03-01"},
            {"Checking Account", "Bank Account", 2500.00, "2024-03-01"},
            {"Investment Portfolio", "Investment", 15000.00, "2024-03-01"},
            {"Real Estate", "Property", 250000.00, "2024-03-01"},
            {"Vehicle", "Asset", 15000.00, "2024-03-01"}
        };

        // Data rows
        for (Object[] rowData : assetData) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < rowData.length; i++) {
                Cell cell = row.createCell(i);
                if (i == 2) { // Value column
                    cell.setCellValue(((Number) rowData[i]).doubleValue());
                    cell.setCellStyle(createCurrencyStyle(workbook));
                } else {
                    cell.setCellValue(rowData[i].toString());
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create the Liabilities sheet with detailed liability information.
     */
    private void createLiabilitiesSheet(Workbook workbook, YearMonth yearMonth, AnalyticsResponse analytics) {
        Sheet sheet = workbook.createSheet("Liabilities");

        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle negativeStyle = createNegativeCurrencyStyle(workbook);

        int rowIndex = 0;

        // Title
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Liabilities - " + yearMonth.toString());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        rowIndex += 2;

        // Headers
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Liability Name", "Type", "Amount", "Interest Rate", "Due Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Sample liability data
        Object[][] liabilityData = {
            {"Mortgage", "Home Loan", 200000.00, 3.5, "2024-04-01"},
            {"Car Loan", "Auto Loan", 15000.00, 4.2, "2024-04-15"},
            {"Credit Card", "Credit", 2500.00, 18.0, "2024-04-10"},
            {"Student Loan", "Education", 35000.00, 5.0, "2024-04-20"}
        };

        // Data rows
        for (Object[] rowData : liabilityData) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < rowData.length; i++) {
                Cell cell = row.createCell(i);
                if (i == 2) { // Amount column
                    cell.setCellValue(((Number) rowData[i]).doubleValue());
                    cell.setCellStyle(negativeStyle);
                } else if (i == 3) { // Interest Rate column
                    cell.setCellValue(((Number) rowData[i]).doubleValue());
                    cell.setCellStyle(createPercentageStyle(workbook));
                } else {
                    cell.setCellValue(rowData[i].toString());
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create the Income sheet with detailed income information.
     */
    private void createIncomeSheet(Workbook workbook, YearMonth yearMonth, AnalyticsResponse analytics) {
        Sheet sheet = workbook.createSheet("Income");

        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle positiveStyle = createPremiumCurrencyStyle(workbook);

        int rowIndex = 0;

        // Title
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Income - " + yearMonth.toString());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowIndex += 2;

        // Headers
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Source", "Category", "Amount", "Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Sample income data
        Object[][] incomeData = {
            {"Salary", "Employment", 5000.00, "2024-03-01"},
            {"Freelance", "Side Business", 1000.00, "2024-03-15"},
            {"Investment", "Dividends", 200.00, "2024-03-20"},
            {"Bonus", "Employment", 1500.00, "2024-03-31"}
        };

        // Data rows
        for (Object[] rowData : incomeData) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < rowData.length; i++) {
                Cell cell = row.createCell(i);
                if (i == 2) { // Amount column
                    cell.setCellValue(((Number) rowData[i]).doubleValue());
                    cell.setCellStyle(positiveStyle);
                } else {
                    cell.setCellValue(rowData[i].toString());
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create the Expenses sheet with detailed expense information.
     */
    private void createExpensesSheet(Workbook workbook, YearMonth yearMonth, AnalyticsResponse analytics) {
        Sheet sheet = workbook.createSheet("Expenses");

        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle negativeStyle = createNegativeCurrencyStyle(workbook);

        int rowIndex = 0;

        // Title
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Expenses - " + yearMonth.toString());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowIndex += 2;

        // Headers
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Category", "Description", "Amount", "Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Sample expense data
        for (var category : analytics.getSpendingByCategory()) {
            Row row = sheet.createRow(rowIndex++);
            Cell categoryCell = row.createCell(0);
            categoryCell.setCellValue(category.getCategoryName());
            categoryCell.setCellStyle(dataStyle);

            Cell descCell = row.createCell(1);
            descCell.setCellValue("Monthly expense");
            descCell.setCellStyle(dataStyle);

            Cell amountCell = row.createCell(2);
            amountCell.setCellValue(category.getTotalAmount().doubleValue());
            amountCell.setCellStyle(negativeStyle);

            Cell dateCell = row.createCell(3);
            dateCell.setCellValue(yearMonth.toString());
            dateCell.setCellStyle(dataStyle);
        }

        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create the Summary sheet matching the monthly expense report template format.
     */
    private void createSummarySheet(Workbook workbook, YearMonth yearMonth, AnalyticsResponse analytics) {
        Sheet sheet = workbook.createSheet("Summary");

        // Create styles
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle totalStyle = createEventTotalStyle(workbook);
        CellStyle categoryStyle = createEventCategoryStyle(workbook);

        int rowIndex = 0;

        // Title
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Monthly Expense Report Summary - " + yearMonth.toString());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

        rowIndex += 2; // Add spacing

        // Subtotals
        Row subtotalRow1 = sheet.createRow(rowIndex++);
        Cell subtotalCell1 = subtotalRow1.createCell(0);
        subtotalCell1.setCellValue("Total Income:");
        subtotalCell1.setCellStyle(subtitleStyle);
        
        Cell subtotalValue1 = subtotalRow1.createCell(1);
        subtotalValue1.setCellValue(analytics.getTotalIncome().doubleValue());
        subtotalValue1.setCellStyle(totalStyle);
        
        Cell subtotalLabel1 = subtotalRow1.createCell(2);
        subtotalLabel1.setCellValue("Total Expenses:");
        subtotalLabel1.setCellStyle(subtitleStyle);
        
        Cell subtotalValue1b = subtotalRow1.createCell(3);
        subtotalValue1b.setCellValue(analytics.getTotalExpenses().doubleValue());
        subtotalValue1b.setCellStyle(totalStyle);

        Row subtotalRow2 = sheet.createRow(rowIndex++);
        Cell subtotalCell2 = subtotalRow2.createCell(0);
        subtotalCell2.setCellValue("Net Savings:");
        subtotalCell2.setCellStyle(subtitleStyle);
        
        Cell subtotalValue2 = subtotalRow2.createCell(1);
        subtotalValue2.setCellValue(analytics.getTotalIncome().subtract(analytics.getTotalExpenses()).doubleValue());
        subtotalValue2.setCellStyle(totalStyle);
        
        Cell subtotalLabel2 = subtotalRow2.createCell(2);
        subtotalLabel2.setCellValue("Save Rate:");
        subtotalLabel2.setCellStyle(subtitleStyle);
        
        Cell subtotalValue2b = subtotalRow2.createCell(3);
        subtotalValue2b.setCellValue(calculateSaveRate(analytics).doubleValue());
        subtotalValue2b.setCellStyle(createPercentageStyle(workbook));

        rowIndex += 2; // Add spacing

        // Headers
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Category", "Category", "Projected", "Actual", "Variance", "Comments"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        rowIndex += 1; // Add spacing

        // Expense Categories matching the template structure
        // Housing
        createBudgetCategory(sheet, rowIndex++, "Housing", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.3)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.3)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Rent/Mortgage", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Utilities", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Transportation
        createBudgetCategory(sheet, rowIndex++, "Transportation", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Car Payment", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Gas", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Food
        createBudgetCategory(sheet, rowIndex++, "Food", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Groceries", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.2)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.2)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Dining Out", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Healthcare
        createBudgetCategory(sheet, rowIndex++, "Healthcare", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Insurance", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.07)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.07)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Medical", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.03)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.03)), 
                        dataStyle, currencyStyle);

        // Software Development
        createBudgetCategory(sheet, rowIndex++, "Software Development", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Tools & Licenses", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.06)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.06)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Training", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.04)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.04)), 
                        dataStyle, currencyStyle);

        // Entertainment
        createBudgetCategory(sheet, rowIndex++, "Entertainment", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Streaming Services", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.03)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.03)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Hobbies", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.02)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.02)), 
                        dataStyle, currencyStyle);

        // Savings
        createBudgetCategory(sheet, rowIndex++, "Savings", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Emergency Fund", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Retirement", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Auto-size columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create the Monthly sheet matching the monthly expense report template format.
     */
    private void createMonthlySheet(Workbook workbook, YearMonth yearMonth, AnalyticsResponse analytics) {
        Sheet sheet = workbook.createSheet(yearMonth.toString());

        // Create styles
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle totalStyle = createEventTotalStyle(workbook);
        CellStyle categoryStyle = createEventCategoryStyle(workbook);

        int rowIndex = 0;

        // Title
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(yearMonth.toString() + " - " + yearMonth.getMonth().name());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

        rowIndex += 2; // Add spacing

        // Subtotals
        Row subtotalRow1 = sheet.createRow(rowIndex++);
        Cell subtotalCell1 = subtotalRow1.createCell(0);
        subtotalCell1.setCellValue("Total Income:");
        subtotalCell1.setCellStyle(subtitleStyle);
        
        Cell subtotalValue1 = subtotalRow1.createCell(1);
        subtotalValue1.setCellValue(analytics.getTotalIncome().doubleValue());
        subtotalValue1.setCellStyle(totalStyle);
        
        Cell subtotalLabel1 = subtotalRow1.createCell(2);
        subtotalLabel1.setCellValue("Total Expenses:");
        subtotalLabel1.setCellStyle(subtitleStyle);
        
        Cell subtotalValue1b = subtotalRow1.createCell(3);
        subtotalValue1b.setCellValue(analytics.getTotalExpenses().doubleValue());
        subtotalValue1b.setCellStyle(totalStyle);

        Row subtotalRow2 = sheet.createRow(rowIndex++);
        Cell subtotalCell2 = subtotalRow2.createCell(0);
        subtotalCell2.setCellValue("Net Savings:");
        subtotalCell2.setCellStyle(subtitleStyle);
        
        Cell subtotalValue2 = subtotalRow2.createCell(1);
        subtotalValue2.setCellValue(analytics.getTotalIncome().subtract(analytics.getTotalExpenses()).doubleValue());
        subtotalValue2.setCellStyle(totalStyle);
        
        Cell subtotalLabel2 = subtotalRow2.createCell(2);
        subtotalLabel2.setCellValue("Save Rate:");
        subtotalLabel2.setCellStyle(subtitleStyle);
        
        Cell subtotalValue2b = subtotalRow2.createCell(3);
        subtotalValue2b.setCellValue(calculateSaveRate(analytics).doubleValue());
        subtotalValue2b.setCellStyle(createPercentageStyle(workbook));

        rowIndex += 2; // Add spacing

        // Headers
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Category", "Category", "Projected", "Actual", "Variance", "Comments"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        rowIndex += 1; // Add spacing

        // Expense Categories matching the template structure
        // Housing
        createBudgetCategory(sheet, rowIndex++, "Housing", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.3)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.3)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Rent/Mortgage", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Utilities", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Transportation
        createBudgetCategory(sheet, rowIndex++, "Transportation", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Car Payment", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Gas", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Food
        createBudgetCategory(sheet, rowIndex++, "Food", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Groceries", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.2)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.2)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Dining Out", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Healthcare
        createBudgetCategory(sheet, rowIndex++, "Healthcare", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Insurance", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.07)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.07)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Medical", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.03)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.03)), 
                        dataStyle, currencyStyle);

        // Software Development
        createBudgetCategory(sheet, rowIndex++, "Software Development", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Tools & Licenses", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.06)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.06)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Training", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.04)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.04)), 
                        dataStyle, currencyStyle);

        // Entertainment
        createBudgetCategory(sheet, rowIndex++, "Entertainment", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Streaming Services", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.03)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.03)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Hobbies", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.02)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.02)), 
                        dataStyle, currencyStyle);

        // Savings
        createBudgetCategory(sheet, rowIndex++, "Savings", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Emergency Fund", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Retirement", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Auto-size columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create the Event Budget sheet matching the professional template format.
     */
    private void createEventBudgetSheet(Workbook workbook, YearMonth yearMonth, AnalyticsResponse analytics) {
        Sheet sheet = workbook.createSheet("Event Budget");

        // Create styles
        CellStyle titleStyle = createEventTitleStyle(workbook);
        CellStyle subtitleStyle = createEventSubtitleStyle(workbook);
        CellStyle headerStyle = createEventHeaderStyle(workbook);
        CellStyle dataStyle = createEventDataStyle(workbook);
        CellStyle currencyStyle = createEventCurrencyStyle(workbook);
        CellStyle totalStyle = createEventTotalStyle(workbook);
        CellStyle categoryStyle = createEventCategoryStyle(workbook);

        int rowIndex = 0;

        // Title
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Event Budget Template Example");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

        rowIndex += 2; // Add spacing

        // Subtotals
        Row subtotalRow1 = sheet.createRow(rowIndex++);
        Cell subtotalCell1 = subtotalRow1.createCell(0);
        subtotalCell1.setCellValue("Projected Subtotal to Date:");
        subtotalCell1.setCellStyle(subtitleStyle);
        
        Cell subtotalValue1 = subtotalRow1.createCell(1);
        subtotalValue1.setCellValue(analytics.getTotalExpenses().doubleValue());
        subtotalValue1.setCellStyle(totalStyle);
        
        Cell subtotalLabel1 = subtotalRow1.createCell(2);
        subtotalLabel1.setCellValue("Projected Subtotal to Date:");
        subtotalLabel1.setCellStyle(subtitleStyle);
        
        Cell subtotalValue1b = subtotalRow1.createCell(3);
        subtotalValue1b.setCellValue(analytics.getTotalExpenses().doubleValue());
        subtotalValue1b.setCellStyle(totalStyle);

        Row subtotalRow2 = sheet.createRow(rowIndex++);
        Cell subtotalCell2 = subtotalRow2.createCell(0);
        subtotalCell2.setCellValue("Actual Subtotal to Date:");
        subtotalCell2.setCellStyle(subtitleStyle);
        
        Cell subtotalValue2 = subtotalRow2.createCell(1);
        subtotalValue2.setCellValue(analytics.getTotalExpenses().doubleValue());
        subtotalValue2.setCellStyle(totalStyle);
        
        Cell subtotalLabel2 = subtotalRow2.createCell(2);
        subtotalLabel2.setCellValue("Actual Subtotal to Date:");
        subtotalLabel2.setCellStyle(subtitleStyle);
        
        Cell subtotalValue2b = subtotalRow2.createCell(3);
        subtotalValue2b.setCellValue(analytics.getTotalExpenses().doubleValue());
        subtotalValue2b.setCellStyle(totalStyle);

        rowIndex += 2; // Add spacing

        // Headers
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Category", "Category", "Projected Subtotal", "Actual Subtotal", "Comments"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        rowIndex += 1; // Add spacing

        // Expense Categories
        // Housing
        createBudgetCategory(sheet, rowIndex++, "Housing", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.3)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.3)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Rent/Mortgage", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Utilities", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Transportation
        createBudgetCategory(sheet, rowIndex++, "Transportation", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Car Payment", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Gas", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Food
        createBudgetCategory(sheet, rowIndex++, "Food", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.25)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Groceries", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.2)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.2)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Dining Out", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Entertainment
        createBudgetCategory(sheet, rowIndex++, "Entertainment", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.1)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Streaming Services", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Hobbies", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Savings
        createBudgetCategory(sheet, rowIndex++, "Savings", "Subtotals", 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.2)), 
                           analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.2)), 
                           categoryStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Emergency Fund", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.15)), 
                        dataStyle, currencyStyle);
        
        createBudgetItem(sheet, rowIndex++, "Retirement", 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        analytics.getTotalExpenses().multiply(BigDecimal.valueOf(0.05)), 
                        dataStyle, currencyStyle);

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create the Event Revenue sheet matching the professional template format.
     */
    private void createEventRevenueSheet(Workbook workbook, YearMonth yearMonth, AnalyticsResponse analytics) {
        Sheet sheet = workbook.createSheet("Event Revenue");

        // Create styles
        CellStyle titleStyle = createEventTitleStyle(workbook);
        CellStyle subtitleStyle = createEventSubtitleStyle(workbook);
        CellStyle headerStyle = createEventHeaderStyle(workbook);
        CellStyle dataStyle = createEventDataStyle(workbook);
        CellStyle currencyStyle = createEventCurrencyStyle(workbook);
        CellStyle totalStyle = createEventTotalStyle(workbook);
        CellStyle categoryStyle = createEventCategoryStyle(workbook);

        int rowIndex = 0;

        // Title
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Event Revenue");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

        rowIndex += 2; // Add spacing

        // Subtotals
        Row subtotalRow1 = sheet.createRow(rowIndex++);
        Cell subtotalCell1 = subtotalRow1.createCell(0);
        subtotalCell1.setCellValue("Projected Subtotal to Date:");
        subtotalCell1.setCellStyle(subtitleStyle);
        
        Cell subtotalValue1 = subtotalRow1.createCell(1);
        subtotalValue1.setCellValue(analytics.getTotalIncome().doubleValue());
        subtotalValue1.setCellStyle(totalStyle);
        
        Cell subtotalLabel1 = subtotalRow1.createCell(2);
        subtotalLabel1.setCellValue("Projected Subtotal to Date:");
        subtotalLabel1.setCellStyle(subtitleStyle);
        
        Cell subtotalValue1b = subtotalRow1.createCell(3);
        subtotalValue1b.setCellValue(analytics.getTotalIncome().doubleValue());
        subtotalValue1b.setCellStyle(totalStyle);

        Row subtotalRow2 = sheet.createRow(rowIndex++);
        Cell subtotalCell2 = subtotalRow2.createCell(0);
        subtotalCell2.setCellValue("Actual Subtotal to Date:");
        subtotalCell2.setCellStyle(subtitleStyle);
        
        Cell subtotalValue2 = subtotalRow2.createCell(1);
        subtotalValue2.setCellValue(analytics.getTotalIncome().doubleValue());
        subtotalValue2.setCellStyle(totalStyle);
        
        Cell subtotalLabel2 = subtotalRow2.createCell(2);
        subtotalLabel2.setCellValue("Actual Subtotal to Date:");
        subtotalLabel2.setCellStyle(subtitleStyle);
        
        Cell subtotalValue2b = subtotalRow2.createCell(3);
        subtotalValue2b.setCellValue(analytics.getTotalIncome().doubleValue());
        subtotalValue2b.setCellStyle(totalStyle);

        rowIndex += 2; // Add spacing

        // Headers
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"Category", "Category", "Quantity", "Quantity", "Cost", "Projected Subtotal", "Projected Subtotal", "Actual Subtotal", "Actual Subtotal", "Comments"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Subheaders
        Row subHeaderRow = sheet.createRow(rowIndex++);
        Cell categoryCell = subHeaderRow.createCell(0);
        categoryCell.setCellValue("Category");
        categoryCell.setCellStyle(headerStyle);
        
        Cell categoryCell2 = subHeaderRow.createCell(1);
        categoryCell2.setCellValue("Category");
        categoryCell2.setCellStyle(headerStyle);
        
        Cell projectedCell = subHeaderRow.createCell(2);
        projectedCell.setCellValue("Projected");
        projectedCell.setCellStyle(headerStyle);
        
        Cell actualCell = subHeaderRow.createCell(3);
        actualCell.setCellValue("Actual");
        actualCell.setCellStyle(headerStyle);
        
        Cell costCell = subHeaderRow.createCell(4);
        costCell.setCellValue("");
        costCell.setCellStyle(headerStyle);
        
        Cell projSubtotalCell = subHeaderRow.createCell(5);
        projSubtotalCell.setCellValue("Subtotal");
        projSubtotalCell.setCellStyle(headerStyle);
        
        Cell projSubtotalCell2 = subHeaderRow.createCell(6);
        projSubtotalCell2.setCellValue("23350");
        projSubtotalCell2.setCellStyle(totalStyle);
        
        Cell actualSubtotalCell = subHeaderRow.createCell(7);
        actualSubtotalCell.setCellValue("Subtotal");
        actualSubtotalCell.setCellStyle(headerStyle);
        
        Cell actualSubtotalCell2 = subHeaderRow.createCell(8);
        actualSubtotalCell2.setCellValue("24200");
        actualSubtotalCell2.setCellStyle(totalStyle);
        
        Cell commentsCell = subHeaderRow.createCell(9);
        commentsCell.setCellValue("Comments");
        commentsCell.setCellStyle(headerStyle);

        rowIndex += 1; // Add spacing

        // Income Categories
        // Salary
        createRevenueCategory(sheet, rowIndex++, "Salary", "Projected", "Actual", "1500", 
                             analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.6)), 
                             analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.6)), 
                             categoryStyle, currencyStyle);
        
        createRevenueItem(sheet, rowIndex++, "Base Salary", "1", "1", "5000", 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.5)), 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.5)), 
                         dataStyle, currencyStyle);
        
        createRevenueItem(sheet, rowIndex++, "Bonus", "1", "1", "1000", 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.1)), 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.1)), 
                         dataStyle, currencyStyle);

        // Investments
        createRevenueCategory(sheet, rowIndex++, "Investments", "Projected", "Actual", "800", 
                             analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.2)), 
                             analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.2)), 
                             categoryStyle, currencyStyle);
        
        createRevenueItem(sheet, rowIndex++, "Dividends", "12", "12", "50", 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.15)), 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.15)), 
                         dataStyle, currencyStyle);
        
        createRevenueItem(sheet, rowIndex++, "Interest", "1", "1", "200", 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.05)), 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.05)), 
                         dataStyle, currencyStyle);

        // Side Business
        createRevenueCategory(sheet, rowIndex++, "Side Business", "Projected", "Actual", "500", 
                             analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.2)), 
                             analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.2)), 
                             categoryStyle, currencyStyle);
        
        createRevenueItem(sheet, rowIndex++, "Freelance", "5", "5", "100", 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.15)), 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.15)), 
                         dataStyle, currencyStyle);
        
        createRevenueItem(sheet, rowIndex++, "Online Sales", "10", "10", "50", 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.05)), 
                         analytics.getTotalIncome().multiply(BigDecimal.valueOf(0.05)), 
                         dataStyle, currencyStyle);

        // Auto-size columns
        for (int i = 0; i < 10; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create the Event Profit Summary sheet matching the professional template format.
     */
    private void createEventProfitSummarySheet(Workbook workbook, YearMonth yearMonth, AnalyticsResponse analytics) {
        Sheet sheet = workbook.createSheet("Event Profit Summary");

        // Create styles
        CellStyle titleStyle = createEventTitleStyle(workbook);
        CellStyle subtitleStyle = createEventSubtitleStyle(workbook);
        CellStyle headerStyle = createEventHeaderStyle(workbook);
        CellStyle dataStyle = createEventDataStyle(workbook);
        CellStyle currencyStyle = createEventCurrencyStyle(workbook);
        CellStyle totalStyle = createEventTotalStyle(workbook);
        CellStyle categoryStyle = createEventCategoryStyle(workbook);

        int rowIndex = 0;

        // Title
        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Event Profit Summary");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowIndex += 2; // Add spacing

        // Budget/Revenue Totals
        Row budgetHeaderRow = sheet.createRow(rowIndex++);
        Cell budgetHeaderCell = budgetHeaderRow.createCell(0);
        budgetHeaderCell.setCellValue("Budget / Revenue Totals");
        budgetHeaderCell.setCellStyle(subtitleStyle);
        
        Cell projectedCell = budgetHeaderRow.createCell(1);
        projectedCell.setCellValue("Projected");
        projectedCell.setCellStyle(headerStyle);
        
        Cell actualCell = budgetHeaderRow.createCell(2);
        actualCell.setCellValue("Actual");
        actualCell.setCellStyle(headerStyle);

        // Total Budget
        Row budgetRow = sheet.createRow(rowIndex++);
        Cell budgetLabel = budgetRow.createCell(0);
        budgetLabel.setCellValue("Total Budget");
        budgetLabel.setCellStyle(dataStyle);
        
        Cell budgetProjected = budgetRow.createCell(1);
        budgetProjected.setCellValue(analytics.getTotalExpenses().doubleValue());
        budgetProjected.setCellStyle(currencyStyle);
        
        Cell budgetActual = budgetRow.createCell(2);
        budgetActual.setCellValue(analytics.getTotalExpenses().doubleValue());
        budgetActual.setCellStyle(currencyStyle);

        // Total Revenue
        Row revenueRow = sheet.createRow(rowIndex++);
        Cell revenueLabel = revenueRow.createCell(0);
        revenueLabel.setCellValue("Total Revenue");
        revenueLabel.setCellStyle(dataStyle);
        
        Cell revenueProjected = revenueRow.createCell(1);
        revenueProjected.setCellValue(analytics.getTotalIncome().doubleValue());
        revenueProjected.setCellStyle(currencyStyle);
        
        Cell revenueActual = revenueRow.createCell(2);
        revenueActual.setCellValue(analytics.getTotalIncome().doubleValue());
        revenueActual.setCellStyle(currencyStyle);

        rowIndex += 2; // Add spacing

        // Profit Totals
        Row profitHeaderRow = sheet.createRow(rowIndex++);
        Cell profitHeaderCell = profitHeaderRow.createCell(0);
        profitHeaderCell.setCellValue("Profit Totals");
        profitHeaderCell.setCellStyle(subtitleStyle);
        
        Cell profitProjectedCell = profitHeaderRow.createCell(1);
        profitProjectedCell.setCellValue("Projected");
        profitProjectedCell.setCellStyle(headerStyle);
        
        Cell profitActualCell = profitHeaderRow.createCell(2);
        profitActualCell.setCellValue("Actual");
        profitActualCell.setCellStyle(headerStyle);

        // Totals
        Row totalsRow = sheet.createRow(rowIndex++);
        Cell totalsLabel = totalsRow.createCell(0);
        totalsLabel.setCellValue("Totals");
        totalsLabel.setCellStyle(dataStyle);
        
        Cell totalsProjected = totalsRow.createCell(1);
        totalsProjected.setCellValue(analytics.getTotalIncome().subtract(analytics.getTotalExpenses()).doubleValue());
        totalsProjected.setCellStyle(totalStyle);
        
        Cell totalsActual = totalsRow.createCell(2);
        totalsActual.setCellValue(analytics.getTotalIncome().subtract(analytics.getTotalExpenses()).doubleValue());
        totalsActual.setCellStyle(totalStyle);

        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create a dashboard card with title and value.
     */
    private void createDashboardCard(Sheet sheet, int startRow, int startCol, String title, BigDecimal value, 
                                   IndexedColors backgroundColor, CellStyle cardTitleStyle, CellStyle cardValueStyle) {
        
        // Create card structure (3x3 merged cells)
        // Title area (top)
        Row titleRow = sheet.createRow(startRow);
        Cell titleCell = titleRow.createCell(startCol);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(cardTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, startCol, startCol + 2));

        // Value area (bottom)
        Row valueRow = sheet.createRow(startRow + 1);
        Cell valueCell = valueRow.createCell(startCol);
        valueCell.setCellValue(formatCurrency(value));
        valueCell.setCellStyle(cardValueStyle);
        sheet.addMergedRegion(new CellRangeAddress(startRow + 1, startRow + 2, startCol, startCol + 2));
    }

    /**
     * Calculate save rate percentage.
     */
    private BigDecimal calculateSaveRate(AnalyticsResponse analytics) {
        if (analytics.getTotalIncome().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal saveRate = analytics.getTotalIncome().subtract(analytics.getTotalExpenses())
            .divide(analytics.getTotalIncome(), 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        return saveRate;
    }

    /**
     * Create a summary card with title and value.
     */
    private void createSummaryCard(Sheet sheet, int startRow, int startCol, String title, String value, 
                                 IndexedColors backgroundColor) {
        
        // Create card structure (3x3 merged cells)
        // Title area (top)
        Row titleRow = sheet.createRow(startRow);
        Cell titleCell = titleRow.createCell(startCol);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(createCardTitleStyle(sheet.getWorkbook(), backgroundColor));
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, startCol, startCol + 2));

        // Value area (bottom)
        Row valueRow = sheet.createRow(startRow + 1);
        Cell valueCell = valueRow.createCell(startCol);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(createCardValueStyle(sheet.getWorkbook(), backgroundColor));
        sheet.addMergedRegion(new CellRangeAddress(startRow + 1, startRow + 2, startCol, startCol + 2));
    }

    // Style creation methods
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 18);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubtitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createCardTitleStyle(Workbook workbook, IndexedColors backgroundColor) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(backgroundColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCardValueStyle(Workbook workbook, IndexedColors backgroundColor) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(backgroundColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createPremiumCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createNegativeCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00_);[Red](#,##0.00)"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createPercentageStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSummaryValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createFormulaStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createFooterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Create a budget category row with subtotal.
     */
    private void createBudgetCategory(Sheet sheet, int rowIndex, String category, String subtotals, 
                                    BigDecimal projected, BigDecimal actual, 
                                    CellStyle categoryStyle, CellStyle currencyStyle) {
        Row row = sheet.createRow(rowIndex);
        
        Cell categoryCell = row.createCell(0);
        categoryCell.setCellValue(category);
        categoryCell.setCellStyle(categoryStyle);
        
        Cell subtotalsCell = row.createCell(1);
        subtotalsCell.setCellValue(subtotals);
        subtotalsCell.setCellStyle(categoryStyle);
        
        Cell projectedCell = row.createCell(2);
        projectedCell.setCellValue(projected.doubleValue());
        projectedCell.setCellStyle(currencyStyle);
        
        Cell actualCell = row.createCell(3);
        actualCell.setCellValue(actual.doubleValue());
        actualCell.setCellStyle(currencyStyle);
    }

    /**
     * Create a budget item row.
     */
    private void createBudgetItem(Sheet sheet, int rowIndex, String item, 
                                BigDecimal projected, BigDecimal actual, 
                                CellStyle dataStyle, CellStyle currencyStyle) {
        Row row = sheet.createRow(rowIndex);
        
        Cell itemCell = row.createCell(0);
        itemCell.setCellValue(item);
        itemCell.setCellStyle(dataStyle);
        
        Cell projectedCell = row.createCell(2);
        projectedCell.setCellValue(projected.doubleValue());
        projectedCell.setCellStyle(currencyStyle);
        
        Cell actualCell = row.createCell(3);
        actualCell.setCellValue(actual.doubleValue());
        actualCell.setCellStyle(currencyStyle);
    }

    /**
     * Create a revenue category row with subtotal.
     */
    private void createRevenueCategory(Sheet sheet, int rowIndex, String category, String projected, String actual, String cost,
                                     BigDecimal projectedTotal, BigDecimal actualTotal,
                                     CellStyle categoryStyle, CellStyle currencyStyle) {
        Row row = sheet.createRow(rowIndex);
        
        Cell categoryCell = row.createCell(0);
        categoryCell.setCellValue(category);
        categoryCell.setCellStyle(categoryStyle);
        
        Cell categoryCell2 = row.createCell(1);
        categoryCell2.setCellValue(category);
        categoryCell2.setCellStyle(categoryStyle);
        
        Cell projectedCell = row.createCell(2);
        projectedCell.setCellValue(projected);
        projectedCell.setCellStyle(categoryStyle);
        
        Cell actualCell = row.createCell(3);
        actualCell.setCellValue(actual);
        actualCell.setCellStyle(categoryStyle);
        
        Cell costCell = row.createCell(4);
        costCell.setCellValue(cost);
        costCell.setCellStyle(categoryStyle);
        
        Cell projTotalCell = row.createCell(5);
        projTotalCell.setCellValue(projectedTotal.doubleValue());
        projTotalCell.setCellStyle(currencyStyle);
        
        Cell projTotalCell2 = row.createCell(6);
        projTotalCell2.setCellValue(projectedTotal.doubleValue());
        projTotalCell2.setCellStyle(currencyStyle);
        
        Cell actualTotalCell = row.createCell(7);
        actualTotalCell.setCellValue(actualTotal.doubleValue());
        actualTotalCell.setCellStyle(currencyStyle);
        
        Cell actualTotalCell2 = row.createCell(8);
        actualTotalCell2.setCellValue(actualTotal.doubleValue());
        actualTotalCell2.setCellStyle(currencyStyle);
    }

    /**
     * Create a revenue item row.
     */
    private void createRevenueItem(Sheet sheet, int rowIndex, String item, String projectedQty, String actualQty, String cost,
                                 BigDecimal projectedTotal, BigDecimal actualTotal,
                                 CellStyle dataStyle, CellStyle currencyStyle) {
        Row row = sheet.createRow(rowIndex);
        
        Cell itemCell = row.createCell(0);
        itemCell.setCellValue(item);
        itemCell.setCellStyle(dataStyle);
        
        Cell itemCell2 = row.createCell(1);
        itemCell2.setCellValue(item);
        itemCell2.setCellStyle(dataStyle);
        
        Cell projectedQtyCell = row.createCell(2);
        projectedQtyCell.setCellValue(projectedQty);
        projectedQtyCell.setCellStyle(dataStyle);
        
        Cell actualQtyCell = row.createCell(3);
        actualQtyCell.setCellValue(actualQty);
        actualQtyCell.setCellStyle(dataStyle);
        
        Cell costCell = row.createCell(4);
        costCell.setCellValue(cost);
        costCell.setCellStyle(dataStyle);
        
        Cell projTotalCell = row.createCell(5);
        projTotalCell.setCellValue(projectedTotal.doubleValue());
        projTotalCell.setCellStyle(currencyStyle);
        
        Cell projTotalCell2 = row.createCell(6);
        projTotalCell2.setCellValue(projectedTotal.doubleValue());
        projTotalCell2.setCellStyle(currencyStyle);
        
        Cell actualTotalCell = row.createCell(7);
        actualTotalCell.setCellValue(actualTotal.doubleValue());
        actualTotalCell.setCellStyle(currencyStyle);
        
        Cell actualTotalCell2 = row.createCell(8);
        actualTotalCell2.setCellValue(actualTotal.doubleValue());
        actualTotalCell2.setCellStyle(currencyStyle);
    }

    // Event-specific style creation methods
    private CellStyle createEventTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createEventSubtitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createEventHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createEventDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createEventCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createEventTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createEventCategoryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private String formatCurrency(BigDecimal value) {
        return String.format("%,.2f", value);
    }

    /**
     * Add a summary card to the PDF table.
     */
    private void addSummaryCard(PdfPTable table, String title, String value, BaseColor backgroundColor) throws DocumentException {
        PdfPTable cardTable = new PdfPTable(1);
        cardTable.setWidthPercentage(100);
        cardTable.setSpacingBefore(5f);
        cardTable.setSpacingAfter(5f);

        // Title cell
        PdfPCell titleCell = new PdfPCell(new Paragraph(title, 
            com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE)));
        titleCell.setBackgroundColor(backgroundColor);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setBorder(Rectangle.BOX);
        titleCell.setBorderWidth(1f);
        titleCell.setPadding(8f);
        cardTable.addCell(titleCell);

        // Value cell
        PdfPCell valueCell = new PdfPCell(new Paragraph(value, 
            com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 14, com.itextpdf.text.BaseColor.WHITE)));
        valueCell.setBackgroundColor(backgroundColor);
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setBorder(Rectangle.BOX);
        valueCell.setBorderWidth(1f);
        valueCell.setPadding(12f);
        cardTable.addCell(valueCell);

        PdfPCell cardCell = new PdfPCell(cardTable);
        cardCell.setBorder(Rectangle.NO_BORDER);
        cardCell.setPadding(5f);
        table.addCell(cardCell);
    }

    /**
     * Add a table header cell with styling.
     */
    private void addTableHeader(PdfPTable table, String text, BaseColor backgroundColor) {
        PdfPCell headerCell = new PdfPCell(new Paragraph(text, 
            com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 12, com.itextpdf.text.BaseColor.WHITE)));
        headerCell.setBackgroundColor(backgroundColor);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerCell.setBorder(Rectangle.BOX);
        headerCell.setBorderWidth(1f);
        headerCell.setPadding(8f);
        table.addCell(headerCell);
    }

    /**
     * Add a table data cell with styling.
     */
    private void addTableCell(PdfPTable table, String text, int alignment, boolean isCurrency) {
        com.itextpdf.text.Font font = isCurrency ? 
            com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.Font.BOLD) :
            com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10);
        
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.5f);
        cell.setPadding(6f);
        table.addCell(cell);
    }
}
