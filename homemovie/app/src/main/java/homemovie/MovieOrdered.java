package homemovie;

public class MovieOrdered extends AbstractEvent {

    private Long wstchId;
    private Long appId;
    private String userId;
    private String movieName;
    private Long movieId;
    private String status;

    public Long getId() {
        return wstchId;
    }

    public void setId(Long wstchId) {
        this.wstchId = wstchId;
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