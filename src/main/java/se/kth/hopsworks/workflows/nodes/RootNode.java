package se.kth.hopsworks.workflows.nodes;

import se.kth.hopsworks.workflows.Node;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class RootNode extends Node {
    public RootNode(){}

}
