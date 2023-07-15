package senti.controller;

import org.springframework.web.bind.annotation.*;
import senti.homeservice.Options;
import senti.homeservice.Response;
import senti.sentistrength.SentiStrength;

@RestController            // <1> 告诉Spring 它是一个Controller
@CrossOrigin
public class HomeController {
  @PostMapping("api/upload")     // <2> 这个方法用来处理来自这个URL的请求
  @ResponseBody
  public Response home(@RequestBody Options option) {
      //System.out.println(option.getFile());
      //System.out.println(option.getOption());
      String [] args=new String[3];
      args[0]="text";
      args[1]=option.getFile();
      args[2]= option.getOption();
      SentiStrength classifier = new SentiStrength();
      classifier.initialiseAndRun(args);
      return Response.buildSuccess(classifier.output);
  }
}
