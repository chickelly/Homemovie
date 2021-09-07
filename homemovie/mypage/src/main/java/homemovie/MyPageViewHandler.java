package homemovie;

import homemovie.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MyPageViewHandler {


    @Autowired
    private MyPageRepository myPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaid_then_CREATE_1 (@Payload Paid paid) {
        try {

            if (!paid.validate()) return;

            System.out.println("\n\n##### listener paid : " + paid.toJson() + "\n\n");

            // view 객체 생성
            MyPage myPage = new MyPage();
            // view 객체에 이벤트의 Value 를 set 함
            myPage.setUserId(paid.getUserId());
            myPage.setMovieId(paid.getAppId());
            myPage.setMovieName(paid.getMovieName());
            myPage.setMovieId(paid.getMovieId());
            myPage.setAppId(paid.getAppId());
            myPage.setStatus(paid.getStatus());
            // view 레파지 토리에 save
            myPageRepository.save(myPage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenMovieOrdered_then_UPDATE_1(@Payload MovieOrdered movieOrdered) {
        try {
            if (!movieOrdered.validate()) return;

             System.out.println("\n\n##### listener movieOrdered : " + movieOrdered.toJson() + "\n\n");

                    // view 객체 조회
                    MyPage myPage = myPageRepository.findByAppId(movieOrdered.getAppId());
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setStatus(movieOrdered.getStatus());
                                        // view 객체에 이벤트의 eventDirectoryValue 를 set 함.
                    myPage.setStatus("Movie Ordered ");
                    // view 레파지 토리에 save
                    myPageRepository.save(myPage);
                
        } catch(Exception e){
            e.printStackTrace();
        }
    }

   @StreamListener(KafkaProcessor.INPUT)
    public void whenMovieAppCancelled_then_UPDATE_2(@Payload MovieAppCancelled movieAppCancelled) {
        try {
            if (!movieAppCancelled.validate()) return;
                // view 객체 조회
            MyPage myPage = myPageRepository.findByAppId(movieAppCancelled.getAppId());
            // view 객체에 이벤트의 eventDirectValue 를 set 함
            myPage.setStatus(movieAppCancelled.getStatus());
            // view 레파지 토리에 save
            myPageRepository.save(myPage);
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

   @StreamListener(KafkaProcessor.INPUT)
    public void whenMovieWatched_then_UPDATE_3(@Payload MovieWatched movieWatched) {
        try {
            if (!movieWatched.validate()) return;
            System.out.println("\n\n##### listener movieWatched : " + movieWatched.toJson() + "\n\n");

                // view 객체 조회
                MyPage myPage = myPageRepository.findByAppId(movieWatched.getAppId());
                // view 객체에 이벤트의 eventDirectoryValue 를 set 함.
                myPage.setStatus("Movie Watched ");
                // view 레파지 토리에 save
                myPageRepository.save(myPage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenMovieCancelled_then_UPDATE_4(@Payload MovieCancelled movieCancelled) {
        try {
            if (!movieCancelled.validate()) return;
            System.out.println("\n\n##### listener movieCancelled : " + movieCancelled.toJson() + "\n\n");

                // view 객체 조회
                MyPage myPage = myPageRepository.findByAppId(movieCancelled.getAppId());
                // view 객체에 이벤트의 eventDirectoryValue 를 set 함.
                myPage.setStatus("Movie Cancelled ");
                // view 레파지 토리에 save
                myPageRepository.save(myPage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

