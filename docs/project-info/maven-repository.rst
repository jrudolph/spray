.. _maven-repo:

Maven Repository
================

Currently the artifacts for all *spray* releases (including milestones and RCs) are available on Maven central.

Nightly builds are available from http://nightlies.spray.io, to use them add this resolver::

  resolvers += "spray nightlies repo" at "http://nightlies.spray.io"


Artifact Naming
---------------

All *spray* artifacts follow this naming scheme:

:Group ID:    ``io.spray``
:Artifact ID: Module Name
:Version:     Release Version


So, for expressing a dependency on a *spray* module with SBT_ you'll want to add something like this
to your project settings::

  libraryDependencies += "io.spray" % "spray-can" % "1.0"

Make sure to replace the artifact name and version number with the one you are targeting! (see :ref:`Current Versions`)


.. _SBT: http://www.scala-sbt.org/
