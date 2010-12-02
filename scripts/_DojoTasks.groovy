def version = "1.4.3"
def srcHref = "http://download.dojotoolkit.org/release-1.4.3/dojo-release-1.4.3-src.zip"
def dojoProfile = "${basedir}/grails-app/conf/dojo.profile.js"
def downloadDir = "${grailsWorkDir}/download"
def tmpWorkingDir = "${basedir}/web-app/js/dojoTmp"
def dojoUtilDir = "${tmpWorkingDir}/util/"
def dojoReleaseDir = "${tmpWorkingDir}/release/dojo"



/**
 * DownloadSource - This will install the source version of Dojo. (About 25MB)
 * It will copy the src to the application. From there it will build the
 * customized version.
 *
 * By copying the source to the application, we can include app specific
 * extensions to our custom release.
 */
target(downloadDojoSource: "This will download the source version of Dojo.") {
  event("StatusUpdate", ["\nDownloading Dojo ${version} source files.\n"])
  Ant.sequential {
    mkdir(dir: downloadDir)
    mkdir(dir:tmpWorkingDir)
    get(dest: "${downloadDir}/dojo-src-${version}.zip", src: "${srcHref}", verbose: true, usetimestamp: true)
    unzip(dest: downloadDir, src: "${downloadDir}/dojo-src-${version}.zip")
  }
  move(todir:tmpWorkingDir){
      fileset(dir: "${downloadDir}/dojo-release-${version}-src", includes: "**/**")    
  }  
  event("StatusFinal", ["\nDojo ${version} source was downloaded and copied to the application.\n"])
}

/**
 * Build Dojo - This will do the same as call the shell script to create the optimized
 *              version of dojo. Thanks to Kevin Haverlock's article:
 *              http://www.ibm.com/developerworks/websphere/techjournal/1003_col_haverlock/1003_col_haverlock.html
 */
target(buildDojo: "This will run shrinksafe to create an optimized version of dojo") {
  depends(downloadDojoSource)
  def shrinksafe_classpath = Ant.path {
    pathelement(location: "${dojoUtilDir}/shrinksafe/js.jar")
    pathelement(location: "${dojoUtilDir}/shrinksafe/shrinksafe.jar")
  }
  java(classname: "org.mozilla.javascript.tools.shell.Main", fork: true, dir: "${dojoUtilDir}/buildscripts", classpath: shrinksafe_classpath) {
    arg(value: "${dojoUtilDir}/buildscripts/build.js")
    arg(value: "profileFile=${dojoProfile}")
    arg(value: "action=release")
    arg(value: "optimize=shrinksafe")
    arg(value: "copyTests=off")
  }
  delete(includeemptydirs: true) {
    fileset(dir: dojoReleaseDir, includes: "**/tests/**/")
    fileset(dir: dojoReleaseDir, includes: "**/demos/**/")
    fileset(dir: dojoReleaseDir, includes: "**/themeTester*")
    fileset(dir: dojoReleaseDir, includes: "**/*.psd")
    fileset(dir: dojoReleaseDir, includes: "**/*.fla")
    fileset(dir: dojoReleaseDir, includes: "**/*.svg")
    fileset(dir: dojoReleaseDir, includes: "**/*.as")
    fileset(dir: dojoReleaseDir, includes: "**/*.swf")
    fileset(dir: dojoReleaseDir, includes: "**/*.uncompressed.js")
  }
  event("StatusFinal", ["\n The customized version of dojo has been created.\n"])
}

/**
 * Will copy the customized dojo release to the staging directory during war
 * creation. This is called from _Events.groovy.
 */
target(copyDojoToStage: "This will copy the optimized dojo release to stage.") {
  event("StatusUpdate", ["Copying optimized dojo release to the staging area."])
  def destinationDir = "${stagingDir}/web-app/js/dojo/${version}"
  copy(todir: "${destinationDir}-custom") {
    fileset(dir: dojoReleaseDir, includes: "**/**")
  }
}

/**
 * Will copy the customized dojo release to the application. This is called
 * from InstallDojo.groovy.
 */
target(copyDojoToApp: "This will copy the optimized dojo release to application.") {
  event("StatusUpdate", ["Copying optimized dojo release to the application."])
  def destinationDir = "${basedir}/web-app/js/dojo/${version}"
  copy(todir: "${destinationDir}-custom") {
    fileset(dir: dojoReleaseDir, includes: "**/**")
  }
}



/**
 * This will delete the tmp Directory.
 */
target(cleanup: "This will copy the optimized dojo release to application.") {
    delete(dir:tmpWorkingDir)
}

