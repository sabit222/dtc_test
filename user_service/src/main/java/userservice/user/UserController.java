package userservice.user;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PatchMapping
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal connectedUser
    ){
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/firstname/{firstname}")
    public ResponseEntity<User> getUserByFirstname(@PathVariable String firstname) {
        return service.getUserByFirstname(firstname)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("User with name " + firstname + " not found"));
    }
}
