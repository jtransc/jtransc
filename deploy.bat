SET JTRANCS_DEPLOY=1
gradlew install uploadArchives publishPlugins -xtest %*
