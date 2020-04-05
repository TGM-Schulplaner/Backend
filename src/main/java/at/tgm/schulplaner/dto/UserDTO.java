package at.tgm.schulplaner.dto;

import at.tgm.schulplaner.model.User;
import lombok.RequiredArgsConstructor;

/**
 * @author Georg Burkl
 * @version 2020-03-30
 */
@RequiredArgsConstructor
public class UserDTO {
    private final User user;

    public String getId(){
        return user.getId().toString();
    }
    public String getEmail(){
        return user.getEmail();
    }
    public String getName(){
        return user.getName();
    }
    public String getType(){
        return user.getType();
    }
    public String getDepartment(){
        return user.getDepartment();
    }
}