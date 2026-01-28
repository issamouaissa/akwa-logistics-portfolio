package org.sid.authentification.controllers;

import org.sid.authentification.configs.JwtUtil;
import org.sid.authentification.dtos.StationDTO;
import org.sid.authentification.entities.Role;
import org.sid.authentification.entities.User;
import org.sid.authentification.repositories.RoleRepository;
import org.sid.authentification.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;


    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user){
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already exists!"));
        }

        // 🛠 Récupérer le rôle réel depuis la base
        Role role = roleRepository.findByName(user.getRole().getName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(role); // associer le vrai rôle depuis la DB
        user.setPassword(passwordEncoder.encode(user.getPassword())); // hacher le mot de passe
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully!"));
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user) {
        Optional<User> foundUser = userRepository.findByUsername(user.getUsername());

        if (foundUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Username not found"));
        }

        boolean passwordMatches = passwordEncoder.matches(user.getPassword(), foundUser.get().getPassword());

        if (passwordMatches) {
            // Générer le token JWT
            String token = jwtUtil.generateToken(foundUser.get().getUsername());

            // Construire la réponse contenant le message, le token, l'id et le rôle
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("id", foundUser.get().getId());
            response.put("role", foundUser.get().getRole().getName()); // Récupérer le rôle sous forme de chaîne de caractères
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Incorrect password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "User logged out successfully!"));
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello! You are authenticated.");
    }


    // ✅ Récupérer tous les utilisateurs
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ Récupérer un utilisateur par ID
    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id);
    }

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId().toString());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole().getName());

            RestTemplate restTemplate = new RestTemplate();

            // 🔁 Station liée à l’utilisateur
            try {
                String stationServiceUrl = "http://localhost:8090/api/stations/user/" + user.getId();
                StationDTO[] stations = restTemplate.getForObject(stationServiceUrl, StationDTO[].class);
                if (stations != null && stations.length > 0) {
                    userInfo.put("station", stations[0]);
                } else {
                    userInfo.put("station", null);
                }
            } catch (Exception e) {
                System.out.println("Erreur station-service : " + e.getMessage());
                userInfo.put("station", null);
            }

            // 🔁 Commandes enrichies de l’utilisateur
            try {
                String commandesUrl = "http://localhost:8090/api/commandes/enriched/user/" + user.getId();
                List<?> commandes = restTemplate.getForObject(commandesUrl, List.class);
                userInfo.put("commandes", commandes);
            } catch (Exception e) {
                System.out.println("Erreur commande-service : " + e.getMessage());
                userInfo.put("commandes", List.of());
            }

            // 🔁 Chauffeur (si le rôle est CHAUFFEUR)
            if ("CHAUFFEUR".equalsIgnoreCase(user.getRole().getName())) {
                try {
                    String chauffeurServiceUrl = "http://localhost:8090/api/chauffeurs/user/" + user.getId();
                    Object chauffeur = restTemplate.getForObject(chauffeurServiceUrl, Object.class);
                    userInfo.put("chauffeur", chauffeur);
                } catch (Exception e) {
                    System.out.println("Erreur chauffeur-service : " + e.getMessage());
                    userInfo.put("chauffeur", null);
                }
            }
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Utilisateur non trouvé"));
        }
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
//        return userRepository.save(user);
        // Hacher le mot de passe avant de l'enregistrer
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    // ✅ Mettre à jour un utilisateur
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(userDetails.getUsername());

            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            // ⚠️ Récupère le rôle depuis la base
            Role role = roleRepository.findByName(userDetails.getRole().getName())
                    .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
            user.setRole(role);

            return ResponseEntity.ok(userRepository.save(user));
        }).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }


    // ✅ Supprimer un utilisateur
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}