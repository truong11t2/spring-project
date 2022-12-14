package project.api.core.user;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserService {

   /**
   * Sample usage, see below.
   *
   * curl -X POST $HOST:$PORT/user \
   *   -H "Content-Type: application/json" --data \
   *   '{"userName":abc,"passWord":123,"firstName":"John","lastName":"Wick","email":"john@mail.com"}'
   *
   * @param body A JSON representation of the new user
   * @return A JSON representation of the newly created user
   */
  @PostMapping(
    value    = "/user",
    consumes = "application/json",
    produces = "application/json")
  User createUser(@RequestBody User body);

  /**
   * Sample usage: "curl $HOST:$PORT/user?userName=abc".
   *
   * @param userName User name of the user
   * @return the user information
   */
  @GetMapping(
    value = "/user",
    produces = "application/json")
  User getUser(@RequestParam(value = "userName", required = true) String userName);

  @GetMapping(
    value = "/user/findUser",
    produces = "application/json"
  )
  Boolean findUserName(@RequestParam(value = "findUser", required = true) String userName);

  @GetMapping(
    value = "/user/findEmail",
    produces = "application/json"
  )
  Boolean findEmail(@RequestParam(value = "findEmail", required = true) String email);
  
  /**
   * Sample usage: "curl -X DELETE $HOST:$PORT/user?userName=abc".
   *
   * @param userName User name of the user
   */
  @DeleteMapping(value = "/user")
  void deleteUser(@RequestParam(value = "userName", required = true)  String userName);

//   /**
//    * Sample usage: "curl $HOST:$PORT/role?roleName=abc".
//    *
//    * @param roleName Role name
//    * @return the name of role
//    */
//   @GetMapping(
//     value = "/role",
//     produces = "application/json")
//   User getRole(@RequestParam(value = "roleName", required = true) String roleName);
}
