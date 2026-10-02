package predictor.demo.AppModules.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Temporary: lets the scheduled job be triggered on demand instead of waiting for the cron slot.
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/scheduler")
public class SchedulerDebugController {

    @Autowired
    private ScheduledPredictionService scheduledPredictionService;

    @PostMapping("/run-now")
    public String runNow() {
        scheduledPredictionService.checkAndConfirmPeriods();
        return "Scheduler run triggered — check predictor.log for results.";
    }
}
