package com.ciamanutencao.production.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ciamanutencao.production.entities.Category;
import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.entities.Technical;
import com.ciamanutencao.production.entities.User;
import com.ciamanutencao.production.enums.UserRole;
import com.ciamanutencao.production.repositories.CategoryRepository;
import com.ciamanutencao.production.repositories.DepartmentRepository;
import com.ciamanutencao.production.repositories.TechnicalRepository;
import com.ciamanutencao.production.repositories.UserRepository;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final TechnicalRepository technicalRepository;

    public DataInitializer(UserRepository userRepository, DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder, CategoryRepository categoryRepository,
            TechnicalRepository technicalRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.technicalRepository = technicalRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {

            // 1. Cria um departamento inicial
            Department adminDept = new Department();
            adminDept.setName("GRCP");
            adminDept.setActive(true);
            adminDept = departmentRepository.save(adminDept);

            // 2. Cria uma Categoria inicial
            Category adminCat = new Category();
            adminCat.setName("CLASSE V");
            adminCat.setActive(true);
            adminCat = categoryRepository.save(adminCat);

            // 3. Cria um tecníco inicial
            Technical adminTec = new Technical();
            adminTec.setName("Mecânico teste");
            adminTec.setActive(true);
            adminTec = technicalRepository.save(adminTec);

            // 4. Cria o usuário ADMIN mestre
            User admin = new User();
            admin.setName("Ailson Souza");
            admin.setLogin("pereira.ailson");
            admin.setPassword(passwordEncoder.encode("33330539.aA@")); // Senha padrão
            admin.setUserRole(UserRole.ROLE_ADMIN);
            admin.setDepartment(adminDept);
            admin.setActive(true);

            userRepository.save(admin);

            System.out.println(">>> BANCO VAZIO DETECTADO: Usuário 'admin' criado com sucesso!");
        }
    }
}