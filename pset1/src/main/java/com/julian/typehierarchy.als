module typehierarchy

abstract sig Type {
    ext: set Type -- ext: Type x Type is a binary relation
}

abstract sig Class extends Type { -- (disjoint) subset
    impl: set Interface -- impl: Class x Interface
}

sig Concrete extends Class {}

sig Abstract extends Class {}

sig Interface extends Type {} -- (disjoint) subset

one sig Object extends Concrete {} -- singleton set
fact { no Object.ext } -- Object.ext is empty set
fact { all c: Class - Object | Object in c.^ext }
fact { no Object.impl } -- Object does not implement interface

-- interface extends interface
fact { all t: Interface | t.ext in Interface }

-- class extends class
fact { all c: Class | c.ext in Class }

-- single inheritance for classes
fact { all c: Class | lone c.ext } -- lone is at most one

-- hierarchy is acyclic
fact { all t: Type | t !in t.^ext } -- ^ is transitive closure

run {} for 4 
