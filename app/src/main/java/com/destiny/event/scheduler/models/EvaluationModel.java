package com.destiny.event.scheduler.models;

import android.os.Parcel;
import android.os.Parcelable;

public class EvaluationModel implements Parcelable {

    private String membershipId;
    private int rate;

    public String getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public EvaluationModel(){
        super();
    }

    public EvaluationModel(Parcel in){
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<EvaluationModel> CREATOR =  new Parcelable.Creator<EvaluationModel>(){
        public EvaluationModel createFromParcel(Parcel in){
            return new EvaluationModel(in);
        }

        @Override
        public EvaluationModel[] newArray(int size) {
            return new EvaluationModel[size];
        }
    };

    private void readFromParcel(Parcel in) {
        membershipId = in.readString();
        rate = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(membershipId);
        dest.writeInt(rate);
    }
}
