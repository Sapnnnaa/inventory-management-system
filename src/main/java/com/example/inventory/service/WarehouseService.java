package com.example.inventory.service;

import com.example.inventory.dto.WarehouseRequest;
import com.example.inventory.entity.Warehouse;
import com.example.inventory.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository){
        this.warehouseRepository = warehouseRepository;
    }

    public Warehouse createWarehouse(WarehouseRequest request){

        Warehouse warehouse = new Warehouse();

        warehouse.setName(request.getName());
        warehouse.setCode(request.getCode());
        warehouse.setLocation(request.getLocation());

        return warehouseRepository.save(warehouse);
    }

    public List<Warehouse> getAllWarehouses(){
        return warehouseRepository.findAll();
    }
}
