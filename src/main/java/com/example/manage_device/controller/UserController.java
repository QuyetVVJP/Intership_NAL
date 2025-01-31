package com.example.manage_device.controller;

import com.example.manage_device.exception.ResourceNotFoundException;
import com.example.manage_device.model.Avatar;
import com.example.manage_device.model.Device;
import com.example.manage_device.model.DeviceLoan;
import com.example.manage_device.model.User;

import com.example.manage_device.service.DeviceLoanService;
import com.example.manage_device.service.DeviceService;
import com.example.manage_device.service.EmailServiceImpl;

import com.example.manage_device.model.dto.UserDto;
import com.example.manage_device.model.request.LoginRequest;
import com.example.manage_device.model.request.UserRequest;
import com.example.manage_device.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.manage_device.utils.ParamKey.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/users")
public class UserController {
    private static String UPLOAD_DIR = "./FE_Manage_Device/src/assets/avatar/";

    @Autowired
    private UserService userService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceLoanService deviceLoanService;
    @Autowired
    private EmailServiceImpl emailService;

    @GetMapping("/list")
    public List<User> getAllUser() {
        List<User> userList = userService.getAllUser();

        return userList;
    }

    @GetMapping("/search")
    public Page<User> search(
            @RequestParam(name = PAGE, required = true, defaultValue = "0") int page,
            @RequestParam(name = PAGE_SIZE, required = true, defaultValue = Integer.MAX_VALUE + "") int size,
            @RequestParam(name = TERM, required = true, defaultValue = "") String term
    ){
        Pageable paging = null;
        paging = PageRequest.of(page, size);

        if (term != null)
            term = term.trim();

        Page<User> resdto = userService.searchByKeyword(term, paging);
        return resdto;
    }

    @GetMapping("/email")
    public String emailService() {
        emailService.sendEmail("doducluong14@gmail.com",
                "Bạn đã đăng kí mượn",
                "Mượn thiết bị <Nal>");
        return  ("Đã gửi mail thành công!");
    }

    @PostMapping("/create")
    public User createUser(@RequestBody User user){
        return userService.save(user);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Boolean>> registerUser(@RequestBody UserRequest userRequest){
        Map<String, Boolean> response = new HashMap<>();
        if(userRequest.getPassword().equals(userRequest.getRePassword()) ){
            userService.register(userRequest);

            response.put("register success", Boolean.TRUE);
            return ResponseEntity.ok(response);
        }
        else {
            response.put("register fail", Boolean.TRUE);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/userIsLogin")
    public UserDto getUserIsLogin(){
        UserDto userDto = new UserDto();
        User user = userService.checkUserIsLogin();
        if (user.getId() != 0){
            userDto.setId(user.getId());
            userDto.setEmail(user.getEmail());
            userDto.setFirst_name(user.getFirst_name());
            userDto.setLast_name(user.getLast_name());
            userDto.setName_role(user.getRole().getName());
            userDto.setAvatar_url(user.getAvatar_url());
        }
        return  userDto;
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout(){
        userService.resetIsLogin();
        Map<String, Boolean> response = new HashMap<>();
        response.put("logout", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        UserDto userDto = new UserDto();
         User user = userService.findByEmail(loginRequest.getEmail());
         if(user != null && user.getPassword().equals(loginRequest.getPassword())){
             userDto.setEmail(user.getEmail());
             userDto.setId(user.getId());
             userDto.setFirst_name(user.getFirst_name());
             userDto.setLast_name(user.getLast_name());
             userDto.setName_role(user.getRole().getName());
             userDto.setAvatar_url(user.getAvatar_url());
             userService.resetIsLogin();
             userService.updateIsLogin(user.getId());
             return ResponseEntity.ok(userDto);
         }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
    }

    @PostMapping("/upload/image/{id}")
    public ResponseEntity<?> uploadAvatar(@PathVariable Long id, @RequestParam("image") MultipartFile file){

        User user = userService.findById(id).get();

        // Create folder to save file if not exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        MultipartFile fileData = file;
        String name = fileData.getOriginalFilename();
        if (name != null && name.length() > 0) {
            try {
                // Create file
                File serverFile = new File(UPLOAD_DIR  + name);
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                stream.write(fileData.getBytes());
                stream.close();
                String avatar_url = serverFile.toString();
                avatar_url = avatar_url.replace("FE_Manage_Device", "..");
                avatar_url = avatar_url.replace("src", "..");
                user.setAvatar_url(avatar_url);
                userService.save(user);
                return ResponseEntity.ok("Success");

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error when uploading");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request");
    }



    @GetMapping("/{id}")
    public ResponseEntity<Optional<User>> getUserById(@PathVariable Long id){
        Optional<User> result = userService.getUserByID(id);
        return  ResponseEntity.ok(result);
    }

    @GetMapping("/get-user-by-device-id/{id}")
    public  ResponseEntity<?> getUserByDeviceId(@PathVariable Long id){
        DeviceLoan deviceLoan = deviceLoanService.getDeviceLoanByDeviceID(id).get();
        User user = userService.findById(deviceLoan.getId()).get();
        return  ResponseEntity.ok(user);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails, @ModelAttribute("avatar") Avatar avatar){
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User khong ton tai"));

        user.setFirst_name(userDetails.getFirst_name());
        user.setLast_name(userDetails.getLast_name());
        user.setGender(userDetails.getGender());
        user.setEmployee_id(userDetails.getEmployee_id());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setDepartment(userDetails.getDepartment());
//        user.setRole_id(userDetails.getRole_id()
        user.setUpdated_at(new Timestamp(System.currentTimeMillis()));
        User updatedUser = userService.save(user);
        return ResponseEntity.ok(updatedUser);
    }



    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable Long id){
        User user = userService.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("User khong ton tai:" + id));
        userService.delete(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
}
