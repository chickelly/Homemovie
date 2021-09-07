package homemovie;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="movieApplications", path="movieApplications")
public interface MovieApplicationRepository extends PagingAndSortingRepository<MovieApplication, Long>{
    MovieApplication findByAppId(Long appId);
}
