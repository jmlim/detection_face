package io.jmlim.faces.detection.apis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comm")
public class CommonController {
    @GetMapping("/ping")
    public void ping() {
    }

    @GetMapping("/time")
    public Map serverTime() {
        return new HashMap() {{
            put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }};
    }
}
