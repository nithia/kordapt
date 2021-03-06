package com.korwe.kordapt.gradle

import com.korwe.kordapt.api.bean.KordaptConfig
import com.korwe.kordapt.gradle.plugin.KordaptGeneratorPlugin
import com.korwe.kordapt.gradle.plugin.KordaptGeneratorPluginContainer
import com.korwe.kordapt.gradle.task.GenerateAll
import com.korwe.kordapt.gradle.task.InitTask
import com.korwe.kordapt.gradle.task.GenerateApiSrc
import com.korwe.kordapt.gradle.task.PullApi
import com.korwe.kordapt.gradle.task.PushApi
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.compile.JavaCompile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.jar.Attributes
import java.util.jar.JarFile

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public class KordaptPlugin implements Plugin<Project> {

    public static KordaptGeneratorPluginContainer pluginContainer = new KordaptGeneratorPluginContainer()
    private static Logger logger = LoggerFactory.getLogger(KordaptPlugin.class)


    void apply(Project project){

        KordaptInit.setup()


        project.apply {
            plugin 'java'
            plugin 'application'
        }

        project.dependencies {
            compile 'com.korwe:kordapt-core:1.0.1'
        }

        project.extensions.create('kordapt', KordaptPluginExtension)

        project.task('kinit', type: InitTask)

        project.kinit.doFirst {
            if(project.kordapt.defaultPackage == null || project.kordapt.defaultPackage.isEmpty()){
                throw new GradleException("You are required to supply a non-empty string for 'defaultPackage'")
            }

            packageName = project.kordapt.defaultPackage
            apiPath = "${project.projectDir.absolutePath}/api-definition"
        }

        project.task('kgenerate', type: GenerateAll, dependsOn: 'compileJava')
        project.task('generateApiSrc', type: GenerateApiSrc)
        project.task('compileApiSrc', type: JavaCompile, dependsOn: 'generateApiSrc'){
            source = project.fileTree "${project.projectDir.absolutePath}/build/tmp/src/main/java"
            destinationDir = project.file "${project.projectDir.absolutePath}/build/tmp/build"
            classpath = project.compileJava.classpath

        }
        project.task('sharedJarFromApi', type: Jar, dependsOn: 'compileApiSrc')
        project.task('tarApi', type: Tar){
            from project.fileTree(dir: "${project.projectDir.absolutePath}/api-definition", includes: ["**/*.yml", '**/*.yaml'])
            archiveName = 'api-definition.tar'
            destinationDir = project.file("${project.projectDir.absolutePath}/build/")
            compression = Compression.NONE
        }
        project.task('pushApi', type: PushApi, dependsOn: 'tarApi'){
            apiPath = project.file("${project.projectDir.absolutePath}/build/api-definition.tar")
        }

        project.task('pullApi', type: PullApi)


        project.generateApiSrc.doFirst{
            if(!project.hasProperty('apiPath') || project.apiPath.isEmpty()){
                project.ext {
                    apiPath = './api-definition'
                }
            }

            if(project.hasProperty('defaultPackage') && !project.defaultPackage.isEmpty()){
                println("Using '${project.defaultPackage}' as default package")
                project.kordapt {
                    defaultPackage = project.defaultPackage
                }
            }
            apiPath = project.apiPath

            kordaptConfig = new KordaptConfig()
            kordaptConfig.mainPath = "${project.projectDir.absolutePath}/build/tmp/src/main"
            kordaptConfig.mainJavaPath = "${kordaptConfig.mainPath}/java"
            kordaptConfig.typePackagePath = "type/package/path" // used as default, but no default
            kordaptConfig.serviceClientPackagePath = "${project.kordapt.defaultPackage.replace('.','/')}/client" // used as default, but no default
            kordaptConfig.defaultTypePackageName = "default.type.package.name" // used as default, but no default

        }

        project.sharedJarFromApi.doFirst{
            from project.fileTree(dir:"${project.projectDir.absolutePath}/build/tmp/build",include: '**/*.class')
            destinationDir=project.file("${project.projectDir.absolutePath}/build/tmp/lib")

        }

        project.kgenerate.doFirst{
            //Setup plugins
            Attributes.Name generatorPluginAttribute = new Attributes.Name(KordaptGeneratorPlugin.MANIFEST_ATTRIBUTE_NAME);
            for(URL url : ((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs()){
                String urlFile = url.getFile();
                String fileExtension = urlFile.substring(urlFile.lastIndexOf(".") + 1, urlFile.length());
                if("file".equals(url.getProtocol())){
                    if("jar".equals(fileExtension)){
                        try {
                            JarFile jarFile = new JarFile(urlFile);
                            if(jarFile.getManifest()!=null){
                                Attributes mainAttributes = jarFile.getManifest().getMainAttributes();

                                if(mainAttributes.containsKey(generatorPluginAttribute)){
                                    String generatorClassName = (String)mainAttributes.get(generatorPluginAttribute);
                                    Class pluginClass = Class.forName(generatorClassName);
                                    if(KordaptGeneratorPlugin.class.isAssignableFrom(pluginClass)){
                                        pluginContainer.registerPlugin((KordaptGeneratorPlugin)pluginClass.newInstance());
                                        logger.info("Registered addon: {}", pluginClass.getName());
                                    }

                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace(); //TODO: Handle jarfile exception
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace(); //TODO: Handle addon class not found
                        } catch (InstantiationException e) {
                            e.printStackTrace(); //TODO: Handle failing instantiation of addonClass
                        } catch (IllegalAccessException e) {
                            e.printStackTrace(); //TODO: Handle failing instantiation of addonClass
                        }

                    }
                }
            }

            if(project.kordapt.defaultPackage == null || project.kordapt.defaultPackage.isEmpty()){
                throw new GradleException("You are required to supply a non-empty string for 'defaultPackage'")
            }

            if(project.kordapt.plugins){

            }

            if(!project.hasProperty('input') || project.input.isEmpty()){
                throw new GradleException("You are required to supply a non-empty string for project parameter 'input'")
            }

            if(project.hasProperty('pkgExt')){
                defaultTypePackageExtension = project.pkgExt
            }
            packageName = project.kordapt.defaultPackage
            apiPath = "${project.projectDir.absolutePath}/api-definition"
            stringInput = project.input




        }

        project.startScripts{
            applicationName = 'services'
        }


        project.mainClassName = 'com.korwe.kordapt.Kordapt'



    }

}

public class KordaptPluginExtension {
    String defaultPackage
    List<String> plugins = []
    List thirdParties = []
}
