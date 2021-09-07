package homemovie;

public class MovieWatched extends AbstractEvent {

    private Long watchId;
    private Long appId;
    private String userId;
    private String movieName;
    private Long movieId;
    private String status;

    public Long getWatchId() {
        return watchId;
    }
    public void setWatchId(Long watchId) {
        this.watchId = watchId;
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