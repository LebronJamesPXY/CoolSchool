package com.pxy.studyhelper.entity;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * User: Pxy(15602269883@163.com)
 * Date: 2016-02-15
 * Time: 23:07
 * FIXME
 */
public class Topic  extends BmobObject implements Serializable{
    private String  userName;
    private String  content;
    private BmobFile image;
    private Integer love=0;
    private String  headUrl;
    private String  userId;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BmobFile getImage() {
        return image;
    }

    public void setImage(BmobFile image) {
        this.image = image;
    }

    public Integer getLove() {
        return love;
    }

    public void setLove(int love) {
        this.love = new Integer(love);
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "userName='" + userName + '\'' +
                ", content='" + content + '\'' +
                ", love=" + love +
                ", headUrl='" + headUrl + '\'' +
                '}';
    }
}
