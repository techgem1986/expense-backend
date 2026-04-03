package com.expenseapp.account.mapper;

import com.expenseapp.account.domain.Account;
import com.expenseapp.account.dto.AccountRequest;
import com.expenseapp.account.dto.AccountResponse;
import org.mapstruct.*;

/**
 * Mapper for Account entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Account toEntity(AccountRequest request);

    @Mapping(source = "user.id", target = "userId")
    AccountResponse toResponse(Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(AccountRequest request, @MappingTarget Account account);
}