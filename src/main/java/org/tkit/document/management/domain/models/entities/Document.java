package org.tkit.document.management.domain.models.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.tkit.document.management.domain.models.enums.LifeCycleState;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The Document entity.
 */
@Getter
@Setter
@Entity
@Table(name = "DM_DOCUMENT")
@NamedEntityGraph(name = "Document.loadAll", includeAllAttributes = true)
public class Document extends TraceableEntity {
    /**
     * Name of the document.
     */
    @Column(name = "NAME")
    private String name;
    /**
     * Description of the document.
     */
    @Column(name = "DESCRIPTION")
    private String description;
    /**
     * The life cycle state of the document.
     */
    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private LifeCycleState lifeCycleState;
    /**
     * Version of the document.
     */
    @Column(name = "VERSION")
    private String documentVersion;
    /**
     * The set of document tags.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "DM_DOCUMENT_TAGS")
    @Column(name = "TAGS")
    private Set<String> tags = new HashSet<>();
    /**
     * The channel.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    @JoinColumn(name = "CHANNEL_GUID")
    private Channel channel;
    /**
     * The document type reference.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_GUID")
    private DocumentType type;
    /**
     * The document specification reference.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "SPECIFICATION_GUID")
    private DocumentSpecification specification;
    /**
     * The related object reference.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "RELATED_OBJECT_GUID")
    private RelatedObjectRef relatedObject;
    /**
     * The document relationship.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "DOCUMENT_GUID")
    private Set<DocumentRelationship> documentRelationships = new HashSet<>();
    /**
     * The document characteristic.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "DOCUMENT_GUID")
    private Set<DocumentCharacteristic> characteristics = new HashSet<>();
    /**
     * The related party reference.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "DOCUMENT_GUID")
    private Set<RelatedPartyRef> relatedParties = new HashSet<>();
    /**
     * The category.
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "DOCUMENT_CATEGORY", joinColumns = @JoinColumn(name = "DOCUMENT_GUID"), inverseJoinColumns = @JoinColumn(name = "CATEGORY_GUID"))
    private Set<Category> categories = new HashSet<>();
    /**
     * The attachment.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "DOCUMENT_GUID")
    private Set<Attachment> attachments = new HashSet<>();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((lifeCycleState == null) ? 0 : lifeCycleState.hashCode());
        result = prime * result + ((documentVersion == null) ? 0 : documentVersion.hashCode());
        result = prime * result + ((tags == null) ? 0 : tags.hashCode());
        result = prime * result + ((channel == null) ? 0 : channel.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((specification == null) ? 0 : specification.hashCode());
        result = prime * result + ((relatedObject == null) ? 0 : relatedObject.hashCode());
        result = prime * result + ((documentRelationships == null) ? 0 : documentRelationships.hashCode());
        result = prime * result + ((characteristics == null) ? 0 : characteristics.hashCode());
        result = prime * result + ((relatedParties == null) ? 0 : relatedParties.hashCode());
        result = prime * result + ((categories == null) ? 0 : categories.hashCode());
        result = prime * result + ((attachments == null) ? 0 : attachments.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Document other = (Document) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (lifeCycleState != other.lifeCycleState)
            return false;
        if (documentVersion == null) {
            if (other.documentVersion != null)
                return false;
        } else if (!documentVersion.equals(other.documentVersion))
            return false;
        if (tags == null) {
            if (other.tags != null)
                return false;
        } else if (!tags.equals(other.tags))
            return false;
        if (channel == null) {
            if (other.channel != null)
                return false;
        } else if (!channel.equals(other.channel))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (specification == null) {
            if (other.specification != null)
                return false;
        } else if (!specification.equals(other.specification))
            return false;
        if (relatedObject == null) {
            if (other.relatedObject != null)
                return false;
        } else if (!relatedObject.equals(other.relatedObject))
            return false;
        if (documentRelationships == null) {
            if (other.documentRelationships != null)
                return false;
        } else if (!documentRelationships.equals(other.documentRelationships))
            return false;
        if (characteristics == null) {
            if (other.characteristics != null)
                return false;
        } else if (!characteristics.equals(other.characteristics))
            return false;
        if (relatedParties == null) {
            if (other.relatedParties != null)
                return false;
        } else if (!relatedParties.equals(other.relatedParties))
            return false;
        if (categories == null) {
            if (other.categories != null)
                return false;
        } else if (!categories.equals(other.categories))
            return false;
        if (attachments == null) {
            if (other.attachments != null)
                return false;
        } else if (!attachments.equals(other.attachments))
            return false;
        return true;
    }
}
