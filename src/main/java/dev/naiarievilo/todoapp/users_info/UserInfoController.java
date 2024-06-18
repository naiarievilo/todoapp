package dev.naiarievilo.todoapp.users_info;

import dev.naiarievilo.todoapp.security.AuthenticatedUser;
import dev.naiarievilo.todoapp.users.User;
import dev.naiarievilo.todoapp.users_info.dtos.UserInfoDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/users/current/info")
@RestController
public class UserInfoController {

    private final UserInfoService userInfoService;

    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public UserInfoDTO getUserInfo(@AuthenticatedUser User user) {
        UserInfo userInfo = userInfoService.getUserInfoById(user.getId());
        return new UserInfoDTO(user.getEmail(), userInfo.getFirstName(), userInfo.getLastName(),
            userInfo.getAvatarUrl());
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateUserInfo(@AuthenticatedUser User user, @RequestBody @Valid UserInfoDTO userInfoDTO) {
        userInfoService.updateUserInfo(user.getId(), userInfoDTO);
    }
}
