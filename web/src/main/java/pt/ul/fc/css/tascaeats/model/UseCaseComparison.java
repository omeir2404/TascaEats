package pt.ul.fc.css.tascaeats.model;

/**
 * Estado de implementação de um caso de uso por interface.
 */
public record UseCaseComparison(
        String code,
        String title,
        String browserStatus,
        String browserNotes,
        String browserCssClass,
        String nativeStatus,
        String nativeNotes,
        String nativeCssClass) {
}

