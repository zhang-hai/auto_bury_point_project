package com.harry.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.google.common.io.Files
import com.harry.bytecode.CallClassAdapter
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

import java.util.concurrent.Callable

class CustomTransform extends Transform {

    Project project
    private WaitableExecutor waitableExecutor;
    private boolean emptyRun = false;

    CustomTransform(Project project) {
        this.project = project
        waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        //当前是否是增量编译(由isIncremental() 方法的返回和当前编译是否有增量基础)
        boolean isIncremental = transformInvocation.isIncremental();
        //消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        //OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();

        //在这里对输入输出的class进行处理
        if(!isIncremental) {
            outputProvider.deleteAll()
        }
        boolean flagForCleanDexBuilderFolder = false;
        for(TransformInput input : inputs) {
            //主要针对jar文件
            /*for(JarInput jarInput : input.getJarInputs()) {
                Status status = jarInput.getStatus();
                File dest = outputProvider.getContentLocation(
                        jarInput.getFile().getAbsolutePath(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(),
                        Format.JAR);
                if(isIncremental && !emptyRun) {
                    switch(status) {
                        case Status.NOTCHANGED:
                            break;
                        case Status.ADDED:
                        case Status.CHANGED:
                            transformJar(jarInput.getFile(), dest, status);
                            break;
                        case Status.REMOVED:
                            if (dest.exists()) {
                                FileUtils.forceDelete(dest);
                            }
                            break;
                    }
                } else {
                    //Forgive me!, Some project will store 3rd-party aar for serveral copies in dexbuilder folder,,unknown issue.
                    if(inDuplcatedClassSafeMode() & !isIncremental && !flagForCleanDexBuilderFolder) {
                        cleanDexBuilderFolder(dest);
                        flagForCleanDexBuilderFolder = true;
                    }
                    transformJar(jarInput.getFile(), dest, status);
                }
            }*/

            for (DirectoryInput directoryInput : input.getDirectoryInputs()){
                File dest = outputProvider.getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(),
                        directoryInput.getScopes(),
                        Format.DIRECTORY)
                FileUtils.forceMkdir(dest)
                if(isIncremental){
                    String srcDirPath = directoryInput.getFile().getAbsolutePath()
                    String destDirPath = dest.getAbsolutePath()
                    Map<File, Status> fileStatusMap = directoryInput.getChangedFiles();
                    for (Map.Entry<File,Status> changedFile : fileStatusMap.entrySet()){
                        Status status = changedFile.getValue()
                        File inputFile = changedFile.getKey()
                        String destFilePath = inputFile.getAbsolutePath().replace(srcDirPath, destDirPath)
                        File destFile = new File(destFilePath)
                        switch (status){
                            case Status.NOTCHANGED:
                                break
                            case Status.REMOVED:
                                if(destFile.exists()){
                                    destFile.delete()
                                }
                                break
                            case Status.ADDED:
                            case Status.CHANGED:
                                try {
                                    FileUtils.touch(destFile)
                                }catch(IOException e){
                                    Files.createParentDirs(destFile)
                                }
                                transformSingleFile(inputFile, destFile, srcDirPath)
                                break
                        }
                    }
                }
            }
        }
    }

    //
    private void transformSingleFile(final File inputFile, final File outputFile, final String srcBaseDir) {
        waitableExecutor.execute(new Callable<Object>() {
            @Override
            Object call() throws Exception {
                weave(inputFile,outputFile)
                return null
            }
        })
    }

    private void transformJar(final File srcJar, final File destJar, Status status) {
        waitableExecutor.execute(new Callable<Object>() {
            @Override
            Object call() throws Exception {
                if(emptyRun) {
                    FileUtils.copyFile(srcJar, destJar);
                    return null;
                }
//                bytecodeWeaver.weaveJar(srcJar, destJar);
                return null
            }
        });
    }

    /**
     * 清理 dexBuilder目录
     */
    private void cleanDexBuilderFolder(File dest) {
        waitableExecutor.execute(new Callable<Object>() {
            @Override
            Object call() throws Exception {
                try {
                    String dexBuilderDir = replaceLastPart(dest.getAbsolutePath(), getName(), "dexBuilder");
                    //intermediates/transforms/dexBuilder/debug
                    File file = new File(dexBuilderDir).getParentFile();
                    project.getLogger().warn("clean dexBuilder folder = " + file.getAbsolutePath());
                    if (file.exists() && file.isDirectory()) {
                        com.android.utils.FileUtils.deleteDirectoryContents(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        })
    }

    //字节码写入
    private void weave(File inputPath, File outputPath) {
        try {
            FileInputStream is = new FileInputStream(inputPath);
            ClassReader cr = new ClassReader(is);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            CallClassAdapter adapter = new CallClassAdapter(cw);
            cr.accept(adapter, 0);ClassReader.SKIP_DEBUG
            FileOutputStream fos = new FileOutputStream(outputPath);
            fos.write(cw.toByteArray());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected RunVariant getRunVariant() {
        return RunVariant.ALWAYS;
    }

    @Override
    Set<QualifiedContent.ContentType> getOutputTypes() {
        return super.getOutputTypes()
    }

    @Override
    Set<? super QualifiedContent.Scope> getReferencedScopes() {
        return TransformManager.EMPTY_SCOPES
    }

    @Override
    public String getName() {
        return "CustomTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    protected boolean inDuplcatedClassSafeMode(){
        return false;
    }

    /**
     * 字符串替换
     * @param originString
     * @param replacement
     * @param toreplace
     * @return
     */
    private String replaceLastPart(String originString, String replacement, String toreplace) {
        int start = originString.lastIndexOf(replacement);
        StringBuilder builder = new StringBuilder();
        builder.append(originString.substring(0, start));
        builder.append(toreplace);
        builder.append(originString.substring(start + replacement.length()));
        return builder.toString();
    }
}