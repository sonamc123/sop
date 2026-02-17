package com.tashicell.sop.Enum;

public enum StageMasterEnum {

    initiated(1, "Initiated"),
    reviewer_verify(2, "Reviewer Verification Needed"),
    endorser_verify(3, "Endorser Verification Needed"),
    reviewer_approve(4, "Approved by Reviewer"),
    reviewer_reject(5, "Rejected by Reviewer"),
    endorser_approve(6, "Approved by Endorser"),
    endorser_reject(7, "Rejected by Endorser"),
    endorser_endorsed(12, "Endorsed by Endorser"),
    pending(9, "Pending to Approved By Committee"),
    focalPersonReview(10, "Focal Person Review"),
    focalPerson_rejected(11, "Rejected By Focal Person"),
    authorizer_approved(8, "Approved by Authorizer"),

    authorizer_rejected(13, "Rejected by Authorizer");


    private final Integer stageId;
    private final String stageName;


    StageMasterEnum(int stageId, String stageName) {
        this.stageId = stageId;
        this.stageName = stageName;

    }

    public Integer getStageId(){
        return stageId;
    }

    public String  getStageName(){
        return stageName;
    }
}
