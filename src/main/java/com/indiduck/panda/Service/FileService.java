package com.indiduck.panda.Service;

import com.indiduck.panda.Repository.FileRepository;
import com.indiduck.panda.domain.File;
import com.indiduck.panda.domain.dto.FileDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Transactional
    public Long saveFile(FileDao fileDto) {
        return fileRepository.save(fileDto.toEntity()).getId();
    }

    @Transactional
    public FileDao getFile(Long id) {
        File file = fileRepository.findById(id).get();

        FileDao fileDao = FileDao.builder()
                .id(id)
                .origFilename(file.getOrigfilename())
                .filename(file.getFilename())
                .filePath(file.getFilepath())
                .build();
        return fileDao;
    }

    @Transactional
    public File getFilebyFilepath(String path){
        File myqueryfind = fileRepository.myqueryfind(path);
        return myqueryfind;
    }
    @Transactional
    public void delFile(String path)
    {
        File myqueryfind = fileRepository.myqueryfind(path);
        myqueryfind.getProduct().delFile(myqueryfind);
        fileRepository.delete(myqueryfind);
    }


}