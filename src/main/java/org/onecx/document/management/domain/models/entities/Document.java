package org.onecx.document.management.domain.models.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.onecx.document.management.domain.models.enums.LifeCycleState;
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

}
