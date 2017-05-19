package dk.os2opgavefordeler.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.User;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

@Repository(forEntity = User.class)
@Transactional
public abstract class UserRepository extends AbstractEntityRepository<User, Long> {

	public abstract User findByEmail(String email);

	public User findByEmailIgnoreCase(String email){
		if(email != null){
			String emailLowerCase = email.toLowerCase();
			return typedQuery("select user from User user where lower(email) = ?1")
					.setParameter(1, emailLowerCase)
					.getSingleResult();
		} else {
			return null;
		}
	}
}