package object Dependency {
  import sbt._
  import Keys._
  // Versions
  object V {
    val jackson  = "[2.1.3,)"
    val typesafe = "[1.0.0,)"
  }
 
  val jacksoncore         = "com.fasterxml.jackson.core"    %   "jackson-core"          % V.jackson
  val jacksonannotations  = "com.fasterxml.jackson.core"    %   "jackson-annotations"   % V.jackson
  val jacksondatabind     = "com.fasterxml.jackson.core"    %   "jackson-databind"      % V.jackson
  val jacksonmodulescala  = "com.fasterxml.jackson.module"  %%  "jackson-module-scala"  % V.jackson

  val typesafeconfig      = "com.typesafe"                  %   "config"                % V.typesafe

}

package object Dependencies {
  import Dependency._
 
  val scalaJSON = Seq(
    jacksoncore, jacksondatabind, jacksonannotations, jacksonmodulescala, typesafeconfig, droolscore
  )
} 
