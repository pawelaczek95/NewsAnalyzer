package Analyzer.model;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
@Table(name = "TAGs")
public class Tag implements Comparable{

    private static final String DUMMY_NAME = "UNNAMED_TAG";

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    private String category;

	@ManyToOne(targetEntity = Country.class, cascade = {CascadeType.ALL})
	@JoinColumn(name = "countryID", referencedColumnName = "ID")
	private Country country;

    @ManyToMany(mappedBy = "tags")
    private Set<PressRelease> pressReleases;

    public Tag(String name, Country country, Set<PressRelease> pressReleases) {
        this.name = name;
        this.country = country;
        this.pressReleases = pressReleases;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Set<PressRelease> getPressReleases() {
        return pressReleases;
    }

    public void setPressReleases(Set<PressRelease> pressReleases) {
        this.pressReleases = pressReleases;
    }

    public Tag() {
        this.name = DUMMY_NAME;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Tag && this.name.equals(((Tag) object).getName());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

    @Override
    public int compareTo(Object o) {
        return this.getName().compareTo(( (Tag) o).getName());
    }
}
