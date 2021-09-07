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
    @Autowired MovieApplicationRepository movieApplicationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMovieCancelled_ChangeStaus(@Payload MovieCancelled movieCancelled){

        if(!movieCancelled.validate()) return;

       System.out.println("\n\n##### listener ChangeStaus : " + movieCancelled.toJson() + "\n\n");

    
       // 상태 변경 - 영화취소됨 //
        MovieApplication movieApplication = movieApplicationRepository.findByAppId(movieCancelled.getAppId());
        movieApplication.setStatus(movieCancelled.getStatus());
        movieApplicationRepository.save(movieApplication);          
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMovieWatched_ChangeStaus(@Payload MovieWatched movieWatched){

        if(!movieWatched.validate()) return;

        System.out.println("\n\n##### listener ChangeStaus : " + movieWatched.toJson() + "\n\n");

        // 상태 변경 - 영화시청함 //
        MovieApplication movieApplication = movieApplicationRepository.findByAppId(movieWatched.getAppId());
        movieApplication.setStatus(movieWatched.getStatus());
        movieApplicationRepository.save(movieApplication);          
    }
 
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMovieOrdered_ChangeStaus(@Payload MovieOrdered movieOrdered){

        if(!movieOrdered.validate()) return;

        System.out.println("\n\n##### listener ChangeStaus : " + movieOrdered.toJson() + "\n\n");

        // 상태 변경 - 영화 구매됨 //
        MovieApplication movieApplication = movieApplicationRepository.findByAppId(movieOrdered.getAppId());
        movieApplication.setStatus(movieOrdered.getStatus());
        movieApplicationRepository.save(movieApplication);          

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
