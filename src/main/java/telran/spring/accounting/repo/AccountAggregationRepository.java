package telran.spring.accounting.repo;

import java.util.List;

import org.bson.Document;

import telran.spring.accounting.entities.AccountEntity;

public interface AccountAggregationRepository {
long getMaxRoles();
List<AccountEntity> getAllAccountsWithMaxRoles();
int getMaxRolesOccurrenceCount();
List<AccountEntity> getAllRolesWithMaxOccurrrence();
int getActiveMinRolesOccurrenceCount();
}
