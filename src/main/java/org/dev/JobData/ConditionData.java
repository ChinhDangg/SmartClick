package org.dev.JobData;

import lombok.Getter;
import lombok.Setter;
import org.dev.Job.Condition.Condition;
import org.dev.Job.MainJob;

import java.io.Serial;

@Getter @Setter
public class ConditionData implements JobData {
    @Serial
    private static final long serialVersionUID = 1L;
    private MainJob condition;
}
