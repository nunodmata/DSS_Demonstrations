package eu.europa.esig.dss.web.repository;

import eu.europa.esig.dss.web.config.MongoConfig;
import eu.europa.esig.dss.web.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;


@Component
@Import(MongoConfig.class)
public class UserRepository {
    @Autowired
    public MongoTemplate mongoTemplate;
    public User save(User user) {
        return mongoTemplate.save(user);
    }

    public User findUserByName(String username){
        return mongoTemplate.findOne(query(where("username").is(username)), User.class);
    }

    public User updateNumbUser(String number, String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        User user = mongoTemplate.findOne(query, User.class);
        user.setPhone_number(number);
        return mongoTemplate.save(user);
    }
    


}
