package dk.os2opgavefordeler.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.Query;

import dk.os2opgavefordeler.model.User;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

@Repository(forEntity = User.class)
@Transactional
public abstract class UserRepository extends AbstractEntityRepository<User, Long> {

	public abstract User findByEmail(String email);

	@Query("select user from User user where lower(email) = lower (?1)")
	public abstract User findByEmailIgnoreCase(String email);

}