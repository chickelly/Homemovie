package homemovie;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="MovieApplication_table")
public class MovieApplication {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long appId;
    private String userId;
    private String movieName;
    private Long movieId;
    private String status;

    @PostPersist
    public void onPostPersist(){
       MoviePicked moviePicked = new MoviePicked();
       BeanUtils.copyProperties(this, moviePicked);
        //moviePicked.setStatus("Moviepicked");
       moviePicked.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        homemovie.external.Payment payment = new homemovie.external.Payment();
        // mappings goes here
    
        payment.setAppId(moviePicked.getAppId());
        payment.setMovieId(moviePicked.getMovieId());
        payment.setMovieName(moviePicked.getMovieName());
        payment.setStatus("Paid");
        payment.setUserId(moviePicked.getUserId());      
        AppApplication.applicationContext.getBean(homemovie.external.PaymentService.class)
            .pay(payment);

    }


    @PostUpdate
    public void onPostUpdate(){
        System.out.println("\n\n##### app onPostUpdate, getStatus() : " + getStatus() + "\n\n");
        if(getStatus().equals("MovieAppCancelled")) {
            MovieAppCancelled movieAppCancelled = new MovieAppCancelled();
            BeanUtils.copyProperties(this, movieAppCancelled);
            //movieAppCancelled.setStatus("MovieAppCancelled");
            movieAppCancelled.publishAfterCommit();
        }
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
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

}