package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    /**
     * 添加菜品
     * @param dishDTO
     */
    @Transactional
    @Override
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dish.setCreateUser(BaseContext.getCurrentId());

        dishMapper.insert(dish);

        List<DishFlavor> flavorList = new ArrayList<>();
        flavorList = dishDTO.getFlavors();
        if(flavorList != null && !flavorList.isEmpty()){
            for (DishFlavor dishFlavor : flavorList) {
                dishFlavor.setDishId(dish.getId());
            }
        }

        dishFlavorMapper.insertBatch(flavorList);
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<Dish> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据菜品id查询
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> flavorList = dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavorList);
        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.update(dish);

        //删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dish.getId());

        //重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        for(Long id:ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.DISABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断当前菜品是否能够删除--是否被套餐关联了
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds != null && !setmealIds.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 起售和停售菜品
     * @param id
     * @param status
     */
    // TODO 菜品停售，则包含菜品的套餐同时停售。
    @Override
    public void startOrStop(Long id, Integer status) {
        dishMapper.updateStatus(id, status);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        return dishMapper.getByCategoryId(categoryId);
    }
}
