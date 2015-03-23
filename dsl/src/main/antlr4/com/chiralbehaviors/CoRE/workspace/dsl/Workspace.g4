grammar Workspace;


workspace:
    definition=workspaceDefinition
    (imports=imported)?
    (agencies = definedAgencies)?
    (attributes = definedAttributes)?
    (intervals = definedIntervals)?
    (locations = definedLocations)?
    (products = definedProducts)?
    (relationships = definedRelationships)?
    (statusCodes = definedStatusCodes)?
    (statusCodeSequencings = definedStatusCodeSequencings)?
    (sequencingAuthorizations = definedSequencingAuthorizations)?
    (units = definedUnits)?
    (edges = definedEdges)?
    EOF;


definedAgencies: 'agencies' LB  (existentialRuleform SC)+ RB;
definedAttributes: 'attributes' LB  (existentialRuleform SC)+ RB;
definedIntervals: 'intervals' LB  (existentialRuleform SC)+ RB;
definedLocations: 'locations' LB  (existentialRuleform SC)+ RB;
definedProducts: 'products' LB  (existentialRuleform SC)+ RB;
definedRelationships: 'relationships' LB  (relationshipPair SC)+ RB;
definedStatusCodes: 'status codes' LB  (existentialRuleform SC)+ RB;
definedStatusCodeSequencings: 'status code sequencings' LB (statusCodeSequencingSet)+ RB;
definedUnits: 'units' LB  (unit SC)+ RB;
definedEdges: 'edges' LB (edge)+ RB;
definedSequencingAuthorizations: 'sequencing auths' LB (selfSequencings)? (parentSequencings)? (siblingSequencings)? (childSequencings)?  RB;
    
workspaceDefinition: 
    'workspace:'
    name=QuotedText
    (description=QuotedText)?;

    
imported:
    'imports' LB (importedWorkspace)+ RB;
    

importedWorkspace: 
    uri =  QuotedText 
    'as '
    namespace = ObjectName;
    
statusCodeSequencingSet:
    service = ObjectName
    ':' LB (sequencePair)+ RB;
    
sequencePair:
    first = ObjectName   
    second = ObjectName;
    
existentialRuleform:
    workspaceName = ObjectName  
    '=' 
    name = QuotedText 
    (description=QuotedText)?; 

unit: 
    existentialRuleform
    datatype = ObjectName
    ('enumerated:' enumerated = Boolean)?
    ('min:' min = Number)?
    ('max:' max = Number)?;
    
relationshipPair:
    primary=existentialRuleform '|' inverse=existentialRuleform; 

qualifiedName:
    (namespace=ObjectName '::')?
    member=ObjectName;
    
edge:
   parent=qualifiedName
   '.' 
   relationship=qualifiedName 
   '.' 
   child=qualifiedName;
   
parentSequencings:
    'parent' LB (parentSequencing)* RB;

parentSequencing:
    'service:' service=qualifiedName
    'status:' status=qualifiedName
    'parent:' parent=qualifiedName
    'next:' next=qualifiedName
    'replace: ' replace=('true' | 'false')
    'activeSiblings:' activeSiblings=('true' | 'false')
    'sequence:' sequenceNumber=Number;
    
siblingSequencings:
    'sibling' LB (siblingSequencing)* RB;

siblingSequencing:
    'parent:' parent=qualifiedName
    'status:' status=qualifiedName
    'sibling:' sibling=qualifiedName
    'next:' next=qualifiedName
    'replace: ' replace=('true' | 'false')
    'sequence:' sequenceNumber=Number;
    
childSequencings:
    'child' LB (childSequencing)* RB;

childSequencing:
    'parent:' parent=qualifiedName
    'status:' status=qualifiedName
    'child:' sibling=qualifiedName
    'next:' next=qualifiedName
    'replace: ' replace=('true' | 'false')
    'sequence:' sequenceNumber=Number;
    
selfSequencings:
    'self' LB (selfSequencing)* RB;

selfSequencing:
    'service:' parent=qualifiedName
    'status:' status=qualifiedName
    'next:' next=qualifiedName
    'sequence:' sequenceNumber=Number;

ObjectName: 'a'..'z' ('A'..'Z' | 'a'..'z' | '0'..'9' | '_')+ ;
QuotedText: '"'(' ' | '!' |'#'.. '~')+ '"';
Boolean: ('true'|'false');
Number: ('0'..'9')+;
 
WS: (' ' | '\t')+ -> skip;
NL: ('\r'? '\n')+ -> skip;
LB: '{';
RB: '}';
SC: ';';