package org.highfives.grid.approval.common.dto;

import lombok.*;
import org.highfives.grid.approval.command.aggregate.ApprovalStatus;
import org.highfives.grid.approval.command.aggregate.YN;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class OvertimeApprovalDTO {

    private int id;

    private String startTime;

    private String endTime;

    private String content;

    private ApprovalStatus approvalStatus;

    private String writeTime;

    private YN cancelYn;

    private int cancelDocId;

    private int requesterId;
}
