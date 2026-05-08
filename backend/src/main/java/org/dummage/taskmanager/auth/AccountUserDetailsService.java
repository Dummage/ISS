package org.dummage.taskmanager.auth;

import org.dummage.taskmanager.user.User;
import org.dummage.taskmanager.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmailIgnoreCase(username.trim().toLowerCase())
				.orElseThrow(() -> new UsernameNotFoundException(username));
		return UserPrincipal.from(user);
	}
}
