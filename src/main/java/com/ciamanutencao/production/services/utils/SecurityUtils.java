package com.ciamanutencao.production.services.utils;

import java.util.Objects;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.entities.User;
import com.ciamanutencao.production.enums.UserRole;

@Component("securityUtils")
public class SecurityUtils {

    public void checkDepartmentAccess(Department department) {
        User user = getAuthenticatedUser();
        if (user.getUserRole() == UserRole.ROLE_ADMIN)
            return;

        Long userDeptId = user.getDepartment().getId();
        boolean allowed = department.getId().equals(userDeptId);

        if (!allowed) {
            throw new AccessDeniedException("Acesso negado: você só pode gerenciar recursos do seu departamento.");
        }
    }

    public void checkDepartmentAccess(Set<Department> departments) {
        User user = getAuthenticatedUser();
        if (user.getUserRole() == UserRole.ROLE_ADMIN)
            return;

        if (departments == null || departments.isEmpty()) {
            throw new AccessDeniedException("Acesso negado: Departamentos não informados.");
        }

        Long userDeptId = user.getDepartment().getId();

        // Verificamos se TODOS os IDs da lista batem com o ID do departamento do Chefe
        boolean allowed = departments.stream()
                .map(Department::getId) // Extraímos apenas o ID para garantir a comparação
                .filter(Objects::nonNull)
                .allMatch(id -> id.equals(userDeptId));

        if (!allowed) {
            throw new AccessDeniedException(
                    "Acesso negado: você só pode gerenciar recursos do SEU departamento (ID: " + userDeptId + ").");
        }
    }

    public void checkUserManagementAccess(Department targetDept, UserRole targetRole) {
        User user = getAuthenticatedUser();

        if (user.getUserRole() == UserRole.ROLE_ADMIN)
            return;

        if (!targetDept.getId().equals(user.getDepartment().getId())) {
            throw new AccessDeniedException("Acesso negado: você só pode gerenciar usuários do seu departamento.");
        }

        if (targetRole != UserRole.ROLE_USER) {
            throw new AccessDeniedException("Chefes só podem gerenciar usuários com nível ROLE_USER.");
        }
    }

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}