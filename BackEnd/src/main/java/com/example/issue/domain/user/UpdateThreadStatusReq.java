package com.example.issue.domain.user;

import com.example.issue.domain.common.ThreadStatus;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

@Data
public class UpdateThreadStatusReq {

    @NotNull
    private ThreadStatus status;
}