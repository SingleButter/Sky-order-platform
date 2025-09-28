package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("添加套餐： {}", setmealDTO);
        setmealService.save(setmealDTO);
        return Result.success();
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询: {}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("查询套餐，id: {}", id);
        SetmealVO setmealVO = setmealService.getByIdWithDishes(id);
        return Result.success(setmealVO);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("批量删除套餐: {}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     * @return
     */
    @PutMapping
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐信息: {}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }
}
