package io.jzheaux.springsecurity.resolutions;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@RestController
public class ResolutionController {
	private final ResolutionRepository resolutions;
	private final UserRepository users;

	public ResolutionController(ResolutionRepository resolutions, UserRepository users) {
		this.resolutions = resolutions;
		this.users = users;
	}

	@CrossOrigin(allowCredentials = "true") //(maxAge = 0) if locally verifying
	@PreAuthorize("hasAuthority('resolution:read')")
	@PostFilter("@post.filter(#root)")
	@GetMapping("/resolutions")
	public Iterable<Resolution> read() {
		Iterable<Resolution> resolutions = this.resolutions.findAll();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if(authentication.getAuthorities().contains(new SimpleGrantedAuthority("user:read"))){

			for (Resolution resolution: resolutions) {
				String fullName = this.users.findByUsername(resolution.getOwner())
						.map(User::getFullName).orElse("Anonymous");
				resolution.setText(resolution.getText() + ", by " + fullName);
			}

		}
		return resolutions;
	}

	@PreAuthorize("hasAuthority('resolution:read')")
	@PostAuthorize("@post.authorize(#root)")
	@GetMapping("/resolution/{id}")
	public Optional<Resolution> read(@PathVariable("id") UUID id) {
		return this.resolutions.findById(id);
	}

	@PreAuthorize("hasAuthority('resolution:write')")
	@PostMapping("/resolution")
	public Resolution make(@CurrentUsername String owner, @RequestBody String text) {
		Resolution resolution = new Resolution(text, owner);
		return this.resolutions.save(resolution);
	}

	@PreAuthorize("hasAuthority('resolution:write')")
	@PostAuthorize("@post.authorize(#root)")
	@PutMapping(path="/resolution/{id}/revise")
	@Transactional
	public Optional<Resolution> revise(@PathVariable("id") UUID id, @RequestBody String text) {
		this.resolutions.revise(id, text);
		return read(id);
	}

	@PreAuthorize("hasAuthority('resolution:write')")
	@PostAuthorize("@post.authorize(#root)")
	@PutMapping("/resolution/{id}/complete")
	@Transactional
	public Optional<Resolution> complete(@PathVariable("id") UUID id) {
		this.resolutions.complete(id);
		return read(id);
	}
}