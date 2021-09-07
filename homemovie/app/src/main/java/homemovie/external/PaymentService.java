package homemovie.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
//profile=default
//@FeignClient(name="payment", url="http://localhost:8083")
//profile=docker
//@FeignClient(name="payment", url="http://payment:8080")
@FeignClient(name="payment", url="${api.url.Payment}")
public interface PaymentService {
    @RequestMapping(method= RequestMethod.POST, path="/payments")
    public void pay(@RequestBody Payment payment);
}

