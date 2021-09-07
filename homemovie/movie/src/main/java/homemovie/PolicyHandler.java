package homemovie;

import homemovie.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired MovieRepository movieRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_OrderMovie(@Payload Paid paid){

        if(!paid.validate()) return;

        System.out.println("\n\n##### listener OrderMovie : " + paid.toJson() + "\n\n");

            // 영화 구매 //
        Movie movie = new Movie();
        movie.setAppId(paid.getAppId());
        movie.setMovieId(paid.getMovieId());
        movie.setMovieName(paid.getMovieName());
        movie.setStatus("MovieOrdered");
        movie.setUserId(paid.getUserId());
        movieRepository.save(movie);       

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMovieAppCancelled_CancelMovie(@Payload MovieAppCancelled movieAppCancelled){

        if(!movieAppCancelled.validate()) return;

        System.out.println("\n\n##### listener CancelMovie : " + movieAppCancelled.toJson() + "\n\n");

            // 수강 취소 //
        Movie movie = movieRepository.findByAppId(movieAppCancelled.getAppId());
        movie.setStatus("MovieCancelled");
        movieRepository.delete(movie);

    }
   
    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}
