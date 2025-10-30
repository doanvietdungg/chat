package chat.jace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/test/websocket")
    public String websocketTest() {
        return "TestSocket";
    }
}
