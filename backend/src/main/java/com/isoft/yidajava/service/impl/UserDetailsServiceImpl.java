package com.isoft.yidajava.service.impl;

import com.isoft.yidajava.entity.SysPermission;
import com.isoft.yidajava.entity.SysUser;
import com.isoft.yidajava.service.SysPermissionService;
import com.isoft.yidajava.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysPermissionService sysPermissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.getUserByUsername(username);

        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        List<SysPermission> permissions = sysPermissionService.getPermissionsByUserId(sysUser.getId());

        List<GrantedAuthority> authorities = permissions.stream()
                .filter(p -> p.getPermCode() != null && !p.getPermCode().isEmpty())
                .map(p -> new SimpleGrantedAuthority(p.getPermCode()))
                .collect(Collectors.toList());

        // ✨ 關鍵修復：Spring Security 嚴格要求不能是空權限
        // 如果該用戶（例如 tech01）目前還沒有被分配任何權限，我們給他一個預設的角色，避免登入崩潰
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
        }

        // ✨ 順便補上狀態檢查，如果帳號被停用(status == 0)，會提示被停用而不是密碼錯誤
        boolean isEnabled = (sysUser.getStatus() != null && sysUser.getStatus() == 1);

        return new User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                isEnabled, // 是否啟用
                true,      // 帳號是否未過期
                true,      // 憑證是否未過期
                true,      // 帳號是否未鎖定
                authorities
        );
    }
}