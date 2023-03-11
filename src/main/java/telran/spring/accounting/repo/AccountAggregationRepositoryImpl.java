package telran.spring.accounting.repo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import org.springframework.stereotype.Repository;
import telran.spring.accounting.entities.AccountEntity;

@Repository
public class AccountAggregationRepositoryImpl implements AccountAggregationRepository {
@Autowired
	MongoTemplate mongoTemplate;
	@Override
	public long getMaxRoles() {
		ArrayList<AggregationOperation> operations = new ArrayList<>();
		operations.add(unwind("roles"));
		operations.add(group("email").count().as("count"));
		operations.add(group().max("count").as("maxCount"));
		Aggregation pipeline = newAggregation(operations);
		var document = mongoTemplate.aggregate(pipeline,
				AccountEntity.class, Document.class);
		return document.getUniqueMappedResult().getInteger("maxCount");
	}
	@Override
	public List<AccountEntity> getAllAccountsWithMaxRoles() {				
		Aggregation pipeline = newAggregation(
						unwind("roles"), 
						group("email").count().as("count"), 
						match(new Criteria("count").is(getMaxRoles()))
						);
		var document = mongoTemplate.aggregate(pipeline, AccountEntity.class, AccountEntity.class);
		return document.getMappedResults();
	}
	@Override
	public int getMaxRolesOccurrenceCount() {		
		Aggregation pipeline = newAggregation(
				unwind("roles"), 
				group("roles").count().as("count"), 
				group().max("count").as("maxCount")				
				);
		var document = mongoTemplate.aggregate(pipeline, AccountEntity.class, Document.class);
		return document.getUniqueMappedResult().getInteger("maxCount");
	}
	@Override
	public List<AccountEntity> getAllRolesWithMaxOccurrrence() {
		
		Aggregation pipeline = newAggregation(
				unwind("roles"), group("roles").count().as("count"), 
				match(new Criteria("count").is(getMaxRolesOccurrenceCount()))
				);
		var document = mongoTemplate.aggregate(pipeline, AccountEntity.class, AccountEntity.class);
		return document.getMappedResults();
	}
	@Override
	public int getActiveMinRolesOccurrenceCount() {
		LocalDateTime localDate = LocalDateTime.now(ZoneId.of("UTC"));
		ArrayList<AggregationOperation> operations = new ArrayList<AggregationOperation>();
		operations.add(match(Criteria.where("expiration").gt(localDate)));
		operations.add(unwind("roles"));
		operations.add(group("roles").count().as("count"));
		operations.add(group().min("count").as("minCount"));
		operations.add(sort(Sort.Direction.ASC, "minCount"));
		operations.add(limit(1));
		Aggregation pipeline = newAggregation(operations);
		var document = mongoTemplate.aggregate(pipeline, AccountEntity.class, Document.class);
		return document.getUniqueMappedResult().getInteger("minCount");
	}

}
