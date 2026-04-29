package pt.ul.fc.css.tascaeats.repository.menuRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.menu.Menu;

public interface IMenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByNomeContainingIgnoreCase(String nome);

    Optional<Menu> findByNomeIgnoreCase(String nome);
}

