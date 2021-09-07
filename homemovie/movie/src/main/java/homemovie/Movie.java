package homemovie;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Movie_table")
public class Movie {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long watchId;
    private String userId;
    private String movieName;
    private Long movieId;
    private String status;
    private Long appId;

    @PostPersist
    public void onPostPersist(){
       /*
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
                e.printStackTrace();
        }
        */
        MovieOrdered movieOrdered = new MovieOrdered();
        BeanUtils.copyProperties(this, movieOrdered);
        movieOrdered.publishAfterCommit();
    }    

    @PostUpdate
    public void onPostUpdate(){
        MovieWatched movieWatched = new MovieWatched();
        BeanUtils.copyProperties(this, movieWatched);
        movieWatched.publishAfterCommit();

        
    }
    
    @PreRemove
    public void onPreRemove(){
        MovieCancelled movieCancelled = new MovieCancelled();
        BeanUtils.copyProperties(this, movieCancelled);
        movieCancelled.publishAfterCommit();


    }


    public Long getWatchId() {
        return watchId;
    }
    public void setWatchId(Long watchId) {
        this.watchId = watchId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMovieName() {
        return movieName;
    }
    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public Long getMovieId() {
        return movieId;
    }
    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAppId() {
        return appId;
    }
    public void setAppId(Long appId) {
        this.appId = appId;
    }


}